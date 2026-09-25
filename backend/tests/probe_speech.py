import asyncio, difflib, json, logging, sys, tempfile
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from app.config import settings
from app.services import tts_service, stt_service
logging.disable(logging.CRITICAL)
async def main():
    settings.MOCK_MODE = False
    output = Path(__file__).resolve().parents[2] / 'docs/live-provider-results.json'
    report = json.loads(output.read_text(encoding='utf-8'))
    report['speech_samples'] = []
    for language, expected in [('en','This bowl is carved by hand from neem wood.'),
        ('hi','यह कटोरा नीम की लकड़ी से हाथ से बनाया गया है।'),
        ('te','ఈ గిన్నెను వేప చెక్కతో చేతితో తయారు చేశాము.')]:
        item={'language':language,'expected':expected}
        try:
            data=await tts_service.synthesize(expected,language)
            if not data or len(data)<44: raise RuntimeError('No audio')
            item['tts']='verified_audio_returned'
            with tempfile.NamedTemporaryFile(suffix='.wav' if data.startswith(b'RIFF') else '.mp3',delete=False) as file:
                file.write(data); path=Path(file.name)
            try:
                actual=await stt_service.transcribe(path,language)
                item.update(actual=actual, similarity=round(difflib.SequenceMatcher(None,expected,actual).ratio(),3),stt='verified_response')
            finally: path.unlink(missing_ok=True)
        except Exception as exc: item['result']=type(exc).__name__
        report['speech_samples'].append(item)
        print(language,item.get('tts'),item.get('stt',item.get('result')),flush=True)
    output.write_text(json.dumps(report,indent=2,ensure_ascii=False),encoding='utf-8')
asyncio.run(main())
