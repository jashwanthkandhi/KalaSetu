import asyncio, hashlib, json, logging, sys, uuid
from pathlib import Path
import httpx
sys.path.insert(0,str(Path(__file__).resolve().parent.parent))
from app.services import supabase_service
logging.disable(logging.CRITICAL)
async def main():
    content=(Path(__file__).parent/'fixtures/pottery_sample.jpg').read_bytes()
    key='verification/'+str(uuid.uuid4())+'.jpg'
    result={}
    try:
        url=await asyncio.to_thread(supabase_service.upload_image,content,key)
        async with httpx.AsyncClient(timeout=15) as client:
            response=await client.get(url)
            result['storage_upload_readback']='verified' if response.status_code==200 and hashlib.sha256(response.content).digest()==hashlib.sha256(content).digest() else 'failed'
    except Exception as exc: result['storage_upload_readback']=type(exc).__name__
    finally:
        try:
            await asyncio.to_thread(lambda:supabase_service.require_client().storage.from_('listing-images').remove([key]))
            result['test_object_cleanup']='requested'
        except Exception as exc: result['test_object_cleanup']=type(exc).__name__
    path=Path(__file__).resolve().parents[2]/'docs/live-storage-results.json'
    path.write_text(json.dumps(result,indent=2),encoding='utf-8')
    print(json.dumps(result))
asyncio.run(main())
