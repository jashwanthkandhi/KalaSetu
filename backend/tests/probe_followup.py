import asyncio, logging, sys, json
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent.parent))
from app.services import llm_service, pricing_service
logging.disable(logging.CRITICAL)
async def main():
    client=llm_service.nim_client or llm_service.openai_client
    actual=client.chat.completions.create
    async def observed(**kwargs):
        try:
            result=await actual(**kwargs)
            print('LLM finish reason:',result.choices[0].finish_reason, 'response length:',len(result.choices[0].message.content or ''),flush=True)
            return result
        except Exception as exc:
            print('LLM provider exception:',type(exc).__name__,flush=True)
            raise
    client.chat.completions.create=observed
    report={}
    try:
        report['textile_listing']=await llm_service.generate_listing('I wove this cotton scarf by hand. It is blue and two meters long.','Textiles','en')
        report['llm']='verified_schema'
    except Exception as exc: report['llm']=type(exc).__name__
    report['pricing']=await asyncio.to_thread(pricing_service.market_guidance,'Wood',['wooden','bowl'],850)
    path=Path(__file__).resolve().parents[2]/'docs/live-followup-results.json'
    path.write_text(json.dumps(report,indent=2,ensure_ascii=False),encoding='utf-8')
    print('LLM:',report['llm'],'Pricing:',report['pricing']['source'],flush=True)
asyncio.run(main())
