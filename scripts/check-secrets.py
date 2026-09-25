"""Scan tracked source and Android bytecode for configured provider secret values.
Print only file paths and variable names, never the values.
"""
import subprocess, zipfile
from pathlib import Path
from dotenv import dotenv_values
root = Path(__file__).resolve().parents[1]
secrets = {}
for env in [root/'.env', root/'backend/.env']:
    if env.exists():
        for key, value in dotenv_values(env).items():
            if value and len(value) >= 16 and any(word in key.upper() for word in ('KEY', 'TOKEN', 'SECRET', 'PASSWORD')):
                secrets[key] = value.encode()
paths = subprocess.check_output(['git','ls-files','--cached','--others','--exclude-standard'], cwd=root, text=True).splitlines()
hits=[]
for name in paths:
    path=root/name
    if not path.is_file(): continue
    data=path.read_bytes()
    for key,value in secrets.items():
        if value in data: hits.append((name,key))
apk = root/'app/build/outputs/apk/debug/app-debug.apk'
if apk.exists():
    with zipfile.ZipFile(apk) as archive:
        for name in archive.namelist():
            if name.endswith(('.dex','.xml','.arsc')):
                data=archive.read(name)
                for key,value in secrets.items():
                    if value in data: hits.append(('APK:'+name,key))
print('Configured secret values checked:', len(secrets))
print('Source files checked:', len(paths))
print('APK inspected:', apk.exists())
print('Secret matches:', hits)
raise SystemExit(bool(hits))
