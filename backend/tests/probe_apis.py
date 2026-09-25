"""Opt-in live probes. Reports statuses and synthetic examples, never credentials."""
import asyncio
import difflib
import json
import logging
import sys
import tempfile
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from app.config import settings
from app.services import stt_service, tts_service, llm_service, category_service, pricing_service, supabase_service

logging.disable(logging.CRITICAL)

async def main():
    settings.MOCK_MODE = False
    report = {'mode': 'live', 'speech_samples': [], 'limitations': [
        'Synthetic TTS speech is not representative of artisan accents or noisy recordings.',
        'ImageNet category mapping is not a fine-tuned Indian craft classifier.']}
    examples = {'en': 'This bowl is carved by hand from neem wood.',
                'hi': 'यह कटोरा नीम की लकड़ी से हाथ से बनाया गया है।',
                'te': 'ఈ గిన్నెను వేప చెక్కతో చేతితో తయారు చేశాము.'}
    for language, expected in examples.items():
        item = {'language': language, 'expected': expected}
        try:
            data = await tts_service.synthesize(expected, language)
            if not data or len(data) < 44:
                raise RuntimeError('tts_unavailable')
            item['tts'] = 'verified_audio_returned'
            suffix = '.wav' if data.startswith(b'RIFF') else '.mp3'
            with tempfile.NamedTemporaryFile(suffix=suffix, delete=False) as audio:
                audio.write(data); path = Path(audio.name)
            try:
                actual = await stt_service.transcribe(path, language)
                item.update(actual=actual, similarity=round(difflib.SequenceMatcher(None, expected, actual).ratio(), 3),
                    stt='verified_response')
            finally:
                path.unlink(missing_ok=True)
        except Exception as exc:
            item['result'] = type(exc).__name__
        report['speech_samples'].append(item)
        print('speech', language, item.get('stt', item.get('result')), flush=True)
    report['llm'] = []
    for category, text in [('Wood', examples['en']), ('Textiles', 'I wove this cotton scarf by hand. It is blue and two meters long.')]:
        try:
            result = await llm_service.generate_listing(text, category, 'en')
            report['llm'].append({'category_input': category, 'result': 'verified_schema', 'listing': result})
        except Exception as exc:
            report['llm'].append({'category_input': category, 'result': type(exc).__name__})
        print('llm', category, report['llm'][-1]['result'], flush=True)
    report['pricing'] = await asyncio.to_thread(pricing_service.market_guidance, 'Wood', ['neem', 'bowl'], 850)
    print('pricing', report['pricing']['source'], flush=True)
    sample = Path(__file__).parent / 'fixtures/pottery_sample.jpg'
    if sample.exists():
        report['classifier'] = await asyncio.to_thread(category_service.predict, sample.read_bytes())
        report['classifier']['model_loaded'] = category_service.model is not None
    for name, operation in [('supabase_read', lambda: supabase_service.require_client().table('products').select('id').limit(1).execute()),
                            ('discovery_schema', lambda: supabase_service.discover(0, 1))]:
        try:
            await asyncio.to_thread(operation)
            report[name] = 'verified_read'
        except Exception as exc:
            report[name] = type(exc).__name__
        print(name, report[name], flush=True)
    report['image_enhancement'] = 'configured_not_yet_probed' if settings.QWEN_IMAGE_ENDPOINT else 'blocked_no_deployed_image_edit_endpoint'
    output = Path(__file__).resolve().parents[2] / 'docs/live-provider-results.json'
    output.write_text(json.dumps(report, indent=2, ensure_ascii=False), encoding='utf-8')
    print('Report saved: docs/live-provider-results.json', flush=True)

if __name__ == '__main__': asyncio.run(main())
