package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.core.model.*
import java.text.DateFormat
import java.text.NumberFormat
import java.util.*

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }
@Composable fun label(text: String): String = translated(text, LocalAppLanguage.current)
fun translated(text: String, language: AppLanguage): String {
    val values = translations[text] ?: return text
    return when(language) { AppLanguage.TELUGU -> values.first; AppLanguage.HINDI -> values.second; else -> text }
}
private val translations = mapOf(
    "Loading photo" to ("ఫోటో లోడ్ అవుతోంది" to "फ़ोटो लोड हो रही है"),
    "Photo unavailable" to ("ఫోటో అందుబాటులో లేదు" to "फ़ोटो उपलब्ध नहीं है"),
    "Product photo" to ("ఉత్పత్తి ఫోటో" to "उत्पाद की फ़ोटो"),
    "Product title" to ("ఉత్పత్తి శీర్షిక" to "उत्पाद का शीर्षक"),
    "Price" to ("ధర" to "कीमत"),
    "Profile photo" to ("ప్రొఫైల్ ఫోటో" to "प्रोफ़ाइल फ़ोटो"),
    "Speech speed: " to ("మాట వేగం: " to "बोलने की गति: "),
    "Legacy example · excluded from insights" to ("పాత ఉదాహరణ · విశ్లేషణల్లో చేర్చలేదు" to "पुराना उदाहरण · विश्लेषण में शामिल नहीं"),
    "Price not set" to ("ధర సెట్ చేయలేదు" to "कीमत तय नहीं है"),
    "This product is unavailable. Return to the catalog." to ("ఈ ఉత్పత్తి అందుబాటులో లేదు. కేటలాగ్‌కు తిరిగి వెళ్లండి." to "यह उत्पाद उपलब्ध नहीं है। कैटलॉग पर लौटें।"),
    "Enhancement unavailable. Your original photo is preserved." to ("ఫోటో మెరుగుదల అందుబాటులో లేదు. మీ అసలు ఫోటో భద్రంగా ఉంది." to "फ़ोटो सुधार उपलब्ध नहीं है। आपकी मूल फ़ोटो सुरक्षित है।"),
    "Complete capture to generate a listing." to ("లిస్టింగ్ రూపొందించడానికి ఫోటో, వాయిస్ పూర్తి చేయండి." to "लिस्टिंग बनाने के लिए फ़ोटो और आवाज़ पूरी करें।"),
    "Created" to ("సృష్టించినది" to "बनाया गया"),
    "Updated" to ("సవరించినది" to "अपडेट किया गया"),
    "Contact: " to ("సంప్రదింపు: " to "संपर्क: "),
    "No phone app is available." to ("ఫోన్ యాప్ అందుబాటులో లేదు." to "फ़ोन ऐप उपलब्ध नहीं है।"),
    "Remove favorite" to ("ఇష్టమైన వాటి నుండి తొలగించు" to "पसंदीदा से हटाएँ"),
    "Save favorite" to ("ఇష్టమైన వాటిలో సేవ్ చేయి" to "पसंदीदा में सहेजें"),
    "Delete this product?" to ("ఈ ఉత్పత్తిని తొలగించాలా?" to "यह उत्पाद हटाएँ?"),
    "This removes the listing and its pending work. Published listings are removed from discovery only after the cloud confirms deletion. This cannot be undone." to ("ఇది లిస్టింగ్ మరియు పెండింగ్ పనిని తొలగిస్తుంది. క్లౌడ్ తొలగింపును నిర్ధారించిన తర్వాతే ప్రచురించిన లిస్టింగ్ డిస్కవరీ నుండి తొలగుతుంది. దీన్ని తిరిగి మార్చలేరు." to "इससे लिस्टिंग और लंबित काम हट जाएगा। क्लाउड से पुष्टि के बाद ही प्रकाशित लिस्टिंग डिस्कवरी से हटेगी। इसे वापस नहीं किया जा सकता।"),
    "Suggested: " to ("సూచించిన ధర: " to "सुझाई गई कीमत: "),
    "Market range: " to ("మార్కెట్ పరిధి: " to "बाज़ार सीमा: "),
    "Handcrafted products. Direct artisan connections." to ("చేతితో చేసిన ఉత్పత్తులు. కళాకారులతో నేరుగా సంబంధం." to "हस्तनिर्मित उत्पाद। कारीगरों से सीधा संपर्क।"),
    "This can take a few minutes on a slow connection." to ("నెమ్మదైన కనెక్షన్‌లో దీనికి కొన్ని నిమిషాలు పట్టవచ్చు." to "धीमे कनेक्शन पर इसमें कुछ मिनट लग सकते हैं।"),
    "Your listing is processing. Please wait for the result." to ("మీ లిస్టింగ్ ప్రాసెస్ అవుతోంది. ఫలితం వచ్చే వరకు వేచి ఉండండి." to "आपकी लिस्टिंग तैयार हो रही है। परिणाम आने तक प्रतीक्षा करें।"),
    "Creating your listing" to ("మీ లిస్టింగ్‌ను రూపొందిస్తోంది" to "आपकी लिस्टिंग तैयार हो रही है"),
    "Your photo and voice note are saved on this device." to ("మీ ఫోటో, వాయిస్ నోట్ ఈ పరికరంలో సేవ్ అయ్యాయి." to "आपकी फ़ोटो और वॉइस नोट इस डिवाइस पर सुरक्षित हैं।"),
    "Record a voice edit" to ("వాయిస్ మార్పును రికార్డ్ చేయండి" to "वॉइस बदलाव रिकॉर्ड करें"),
    "Original voice note" to ("అసలు వాయిస్ నోట్" to "मूल वॉइस नोट"),
    "Choose an action" to ("ఒక చర్యను ఎంచుకోండి" to "कोई कार्रवाई चुनें"),
    "Improve title" to ("శీర్షికను మెరుగుపరచండి" to "शीर्षक सुधारें"),
    "Improve description" to ("వివరణను మెరుగుపరచండి" to "विवरण सुधारें"),
    "Simplify description" to ("వివరణను సరళీకరించండి" to "विवरण सरल करें"),
    "Generate tags" to ("ట్యాగ్‌లను రూపొందించండి" to "टैग बनाएं"),
    "Translate to selected language" to ("ఎంచుకున్న భాషలోకి అనువదించండి" to "चुनी हुई भाषा में अनुवाद करें"),
    "Suggest price" to ("ధరను సూచించండి" to "कीमत सुझाएं"),
    "Propose edit" to ("మార్పును సూచించండి" to "बदलाव सुझाएं"),
    "Propose voice edit" to ("వాయిస్ మార్పును సూచించండి" to "वॉइस बदलाव सुझाएं"),
    "Enhancement unavailable. The original is preserved." to ("ఫోటో మెరుగుదల అందుబాటులో లేదు. అసలు ఫోటో భద్రంగా ఉంది." to "फ़ोटो सुधार उपलब्ध नहीं है। मूल फ़ोटो सुरक्षित है।"),
    "Compare the photos and make sure your product has not changed." to ("ఫోటోలను పోల్చి మీ ఉత్పత్తి మారలేదని నిర్ధారించండి." to "फ़ोटो की तुलना करके सुनिश्चित करें कि आपका उत्पाद बदला नहीं है।"),
    "Craft details" to ("కళా వివరాలు" to "शिल्प विवरण"),
    "AI assistant" to ("AI సహాయకుడు" to "AI सहायक"),
    "Suggestions are shown for approval before they change your listing." to ("మీ లిస్టింగ్‌లో మార్పు చేసే ముందు సూచనలు మీ ఆమోదం కోసం చూపబడతాయి." to "लिस्टिंग बदलने से पहले सुझाव आपकी मंज़ूरी के लिए दिखाए जाते हैं।"),
    "Suggested" to ("సూచించిన" to "सुझाया गया"),
    "Profile" to ("ప్రొఫైల్" to "प्रोफ़ाइल"),
    "Give your craft a name and a story." to ("మీ కళకు పేరు, కథ ఇవ్వండి." to "अपनी कला को नाम और कहानी दें।"),
    "Your name, shop, craft, location and bio are included when you publish. Phone is shared only with your permission. Email and profile photo remain on this device." to ("మీరు ప్రచురించినప్పుడు మీ పేరు, దుకాణం, కళ, ప్రాంతం, పరిచయం చేర్చబడతాయి. మీ అనుమతితో మాత్రమే ఫోన్ పంచబడుతుంది. ఇమెయిల్, ప్రొఫైల్ ఫోటో ఈ పరికరంలోనే ఉంటాయి." to "प्रकाशित करते समय आपका नाम, दुकान, शिल्प, स्थान और परिचय शामिल होंगे। फ़ोन आपकी अनुमति से ही साझा होगा। ईमेल और प्रोफ़ाइल फ़ोटो इसी डिवाइस पर रहेंगी।"),
    "Make KalaSetu comfortable for you." to ("కళాసేతును మీకు అనుకూలంగా మార్చుకోండి." to "कलासेतु को अपनी सुविधा के अनुसार बदलें।"),
    "Photos and recordings are stored privately on this device. Processing sends them to the backend and configured AI services. Original product photos are stored in cloud storage; product images may be publicly accessible by URL. Audio is deleted from backend temporary storage after processing. Published listings expose only the public profile you chose. Uninstalling clears local drafts, preferences and the installation key used to manage your cloud listings." to ("ఫోటోలు, రికార్డింగ్‌లు ఈ పరికరంలో ప్రైవేట్‌గా నిల్వ ఉంటాయి. ప్రాసెసింగ్ కోసం అవి బ్యాకెండ్, ఎంచుకున్న AI సేవలకు పంపబడతాయి. అసలు ఉత్పత్తి ఫోటోలు క్లౌడ్‌లో నిల్వ ఉంటాయి; ఉత్పత్తి చిత్రాలు URL ద్వారా అందుబాటులో ఉండవచ్చు. ప్రాసెసింగ్ తర్వాత ఆడియో తొలగించబడుతుంది. ప్రచురించిన లిస్టింగ్‌లు మీరు ఎంచుకున్న పబ్లిక్ ప్రొఫైల్‌ను మాత్రమే చూపుతాయి. యాప్ తొలగిస్తే స్థానిక డ్రాఫ్ట్‌లు, ప్రాధాన్యతలు, క్లౌడ్ లిస్టింగ్‌ల నిర్వహణ కీ తొలగుతాయి." to "फ़ोटो और रिकॉर्डिंग इस डिवाइस पर निजी रूप से रखी जाती हैं। प्रोसेसिंग के लिए वे बैकएंड और चुनी गई AI सेवाओं को भेजी जाती हैं। मूल उत्पाद फ़ोटो क्लाउड में रखी जाती हैं; उत्पाद चित्र URL से सार्वजनिक हो सकते हैं। प्रोसेसिंग के बाद ऑडियो हटा दिया जाता है। प्रकाशित लिस्टिंग में आपके चुने हुए सार्वजनिक प्रोफ़ाइल की जानकारी ही दिखती है। ऐप हटाने पर स्थानीय ड्राफ़्ट, प्राथमिकताएँ और क्लाउड लिस्टिंग प्रबंधन कुंजी हट जाती है।"),
    "How it works\n1. Take or choose a photo.\n2. Record up to 29 seconds about your craft.\n3. Review AI suggestions, photos and price.\n4. Confirm to publish.\n\nNo connection? Your capture waits safely in the sync center. Failed work can be retried. AI may be wrong; always check material, size and price." to ("ఇది ఎలా పనిచేస్తుంది\n1. ఫోటో తీయండి లేదా ఎంచుకోండి.\n2. మీ కళ గురించి 29 సెకన్ల వరకు రికార్డ్ చేయండి.\n3. AI సూచనలు, ఫోటోలు, ధరను సమీక్షించండి.\n4. ప్రచురించడానికి నిర్ధారించండి.\n\nకనెక్షన్ లేదా? మీ క్యాప్చర్ సింక్ కేంద్రంలో సురక్షితంగా వేచి ఉంటుంది. విఫలమైన పనిని మళ్లీ ప్రయత్నించవచ్చు. AI తప్పు కావచ్చు; పదార్థం, పరిమాణం, ధరను తప్పకుండా తనిఖీ చేయండి." to "यह कैसे काम करता है\n1. फ़ोटो लें या चुनें।\n2. अपने शिल्प के बारे में 29 सेकंड तक रिकॉर्ड करें।\n3. AI सुझाव, फ़ोटो और कीमत की समीक्षा करें।\n4. प्रकाशित करने के लिए पुष्टि करें।\n\nकनेक्शन नहीं है? आपका कैप्चर सिंक केंद्र में सुरक्षित रहेगा। असफल काम फिर से किया जा सकता है। AI गलत हो सकता है; सामग्री, आकार और कीमत ज़रूर जाँचें।"),
    "Report a problem" to ("సమస్యను నివేదించండి" to "समस्या की रिपोर्ट करें"),
    "A support address has not been configured. Share the issue report with your project contact." to ("సహాయ చిరునామా ఇంకా సెట్ కాలేదు. సమస్య నివేదికను ప్రాజెక్ట్ సంప్రదింపుతో పంచుకోండి." to "सहायता पता अभी सेट नहीं है। समस्या रिपोर्ट अपने प्रोजेक्ट संपर्क को भेजें।"),
    "An AI catalog and discovery assistant for Indian artisans." to ("భారతీయ కళాకారుల కోసం AI కేటలాగ్, డిస్కవరీ సహాయకుడు." to "भारतीय कारीगरों के लिए AI कैटलॉग और खोज सहायक।"),
    "No work waiting for sync" to ("సింక్ కోసం పని ఏదీ వేచి లేదు" to "सिंक के लिए कोई काम लंबित नहीं है"),
    "need attention" to ("శ్రద్ధ అవసరం" to "ध्यान दें"),
    "You have " to ("మీ వద్ద " to "आपके पास "),
    " drafts to review. Your products publish only after you confirm." to (" డ్రాఫ్ట్‌లు సమీక్షించాలి. మీరు నిర్ధారించిన తర్వాతే ఉత్పత్తులు ప్రచురిస్తాం." to " ड्राफ़्ट की समीक्षा करनी है। आपकी पुष्टि के बाद ही उत्पाद प्रकाशित होंगे।"),
    "Photo tip: use daylight and a simple background to show the texture of your craft." to ("ఫోటో సూచన: మీ కళ ఆకృతిని చూపించడానికి పగటి వెలుతురు, సరళమైన నేపథ్యాన్ని ఉపయోగించండి." to "फ़ोटो सुझाव: अपने शिल्प की बनावट दिखाने के लिए दिन की रोशनी और साधारण पृष्ठभूमि रखें।"),
    "Start with a photo and a short voice note about your craft." to ("మీ కళ గురించి ఫోటో, చిన్న వాయిస్ నోట్‌తో ప్రారంభించండి." to "अपने शिल्प की फ़ोटो और छोटे वॉइस नोट से शुरुआत करें।"),
    "Try another search or change your filters." to ("మరో శోధన ప్రయత్నించండి లేదా ఫిల్టర్‌లను మార్చండి." to "कोई और खोज आज़माएँ या फ़िल्टर बदलें।"),
    "Published artisan products will appear here. Try another search or return later." to ("ప్రచురించిన కళాకారుల ఉత్పత్తులు ఇక్కడ కనిపిస్తాయి. మరో శోధన ప్రయత్నించండి లేదా తర్వాత తిరిగి రండి." to "प्रकाशित कारीगर उत्पाद यहाँ दिखेंगे। कोई और खोज आज़माएँ या बाद में लौटें।"),
    "Your collection starts with one creation." to ("మీ సేకరణ ఒక సృష్టితో ప్రారంభమవుతుంది." to "आपका संग्रह एक रचना से शुरू होता है।"),
    "products created in the last 7 days" to ("గత 7 రోజుల్లో సృష్టించిన ఉత్పత్తులు" to "पिछले 7 दिनों में बनाए गए उत्पाद"),
    "A clear view of your work on this device." to ("ఈ పరికరంలోని మీ పనికి స్పష్టమైన దృశ్యం." to "इस डिवाइस पर आपके काम का साफ़ दृश्य।"),
    "Categories used" to ("ఉపయోగించిన వర్గాలు" to "उपयोग की गई श्रेणियाँ"),
    "Languages used" to ("ఉపయోగించిన భాషలు" to "उपयोग की गई भाषाएँ"),
    "Buyer views, orders and sales are not tracked. These insights use your real catalog records only." to ("కొనుగోలుదారుల వీక్షణలు, ఆర్డర్లు, అమ్మకాలు ట్రాక్ చేయబడవు. ఈ విశ్లేషణలు మీ నిజమైన కేటలాగ్ రికార్డులను మాత్రమే ఉపయోగిస్తాయి." to "खरीदारों के व्यू, ऑर्डर और बिक्री ट्रैक नहीं किए जाते। ये जानकारी केवल आपके असली कैटलॉग रिकॉर्ड से आती है।"),
    "Attempts: " to ("ప్రయత్నాలు: " to "प्रयास: "),
    "Return to capture" to ("క్యాప్చర్‌కు తిరిగి వెళ్లండి" to "कैप्चर पर लौटें"),
    "Select a draft to review." to ("సమీక్షించడానికి డ్రాఫ్ట్‌ను ఎంచుకోండి." to "समीक्षा के लिए ड्राफ़्ट चुनें।"),
    "Back" to ("వెనుకకు" to "वापस"),
    "AI Generated" to ("AI రూపొందించినది" to "AI जनरेटेड"),
    "Review before saving — this is AI generated" to ("సేవ్ చేయడానికి ముందు సమీక్షించండి — ఇది AI రూపొందించింది" to "सहेजने से पहले समीक्षा करें — इसे AI ने बनाया है"),
    "Separate tags with commas" to ("ట్యాగ్‌లను కామాలతో వేరు చేయండి" to "टैग को अल्पविराम से अलग करें"),
    "Describe an edit" to ("ఏ మార్పు కావాలో చెప్పండి" to "बदलाव बताएं"),
    "Review proposed changes" to ("సూచించిన మార్పులను సమీక్షించండి" to "सुझाए गए बदलावों की समीक्षा करें"),
    "Your selling price remains " to ("మీ అమ్మకపు ధర అలాగే ఉంటుంది: " to "आपकी बिक्री कीमत बनी रहेगी: "),
    "Change it yourself before publishing." to ("ప్రచురించే ముందు మీరే మార్చండి." to "प्रकाशित करने से पहले इसे खुद बदलें।"),
    "Needs attention" to ("శ్రద్ధ అవసరం" to "ध्यान दें"),
    "No work waiting for sync." to ("సింక్ కోసం పని ఏదీ వేచి లేదు." to "सिंक के लिए कोई काम लंबित नहीं है।"),
    "Photos and voice notes stay on this device while processing is pending. Generated listings wait for your review." to ("ప్రాసెసింగ్ పెండింగ్‌లో ఉన్నప్పుడు ఫోటోలు, వాయిస్ నోట్లు ఈ పరికరంలోనే ఉంటాయి. రూపొందించిన లిస్టింగ్‌లు మీ సమీక్ష కోసం వేచి ఉంటాయి." to "प्रोसेसिंग लंबित रहने तक फ़ोटो और वॉइस नोट इसी डिवाइस पर रहते हैं। बनाई गई लिस्टिंग आपकी समीक्षा की प्रतीक्षा करती हैं।"),
    "No work waiting for sync" to ("సింక్ కోసం పని ఏదీ వేచి లేదు" to "सिंक के लिए कोई काम लंबित नहीं है"),
    "Allow device notifications" to ("పరికర నోటిఫికేషన్‌లను అనుమతించండి" to "डिवाइस सूचनाओं की अनुमति दें"),
    "Follow app language" to ("యాప్ భాషను అనుసరించండి" to "ऐप की भाषा का पालन करें"),
    "Profile saved. Future publishing uses these details." to ("ప్రొఫైల్ సేవ్ అయింది. భవిష్యత్ ప్రచురణలో ఈ వివరాలు ఉపయోగిస్తాం." to "प्रोफ़ाइल सहेजी गई। आगे प्रकाशित करते समय ये विवरण उपयोग होंगे।"),
    "Camera unavailable. Choose a photo instead." to ("కెమెరా అందుబాటులో లేదు. బదులుగా ఫోటో ఎంచుకోండి." to "कैमरा उपलब्ध नहीं है। इसके बजाय फ़ोटो चुनें।"),
    "Microphone permission is needed to record your description." to ("మీ వివరణను రికార్డ్ చేయడానికి మైక్రోఫోన్ అనుమతి అవసరం." to "आपका विवरण रिकॉर्ड करने के लिए माइक्रोफ़ोन की अनुमति ज़रूरी है।"),
    "Draft" to ("చిత్తుప్రతి" to "ड्राफ़्ट"), "Archived" to ("భద్రపరచినవి" to "संग्रहित"),
    "Pending Upload" to ("అప్‌లోడ్ పెండింగ్" to "अपलोड बाकी"), "Processing" to ("ప్రాసెస్ అవుతోంది" to "तैयार हो रहा है"),
    "Pottery" to ("కుండలు" to "मिट्टी के बर्तन"), "Textiles" to ("వస్త్రాలు" to "वस्त्र"),
    "Bamboo" to ("వెదురు" to "बाँस"), "Wood" to ("చెక్క" to "लकड़ी"),
    "Home Decor" to ("ఇంటి అలంకరణ" to "घर की सजावट"), "Jewellery" to ("ఆభరణాలు" to "आभूषण"),
    "Paintings" to ("చిత్రాలు" to "चित्रकारी"), "Leather" to ("తోలు" to "चमड़ा"), "Other" to ("ఇతర" to "अन्य"),
    "Make room for your next creation." to ("మీ తదుపరి సృష్టికి సిద్ధం కండి." to "अपनी अगली रचना के लिए तैयार हों।"),
    "Your craft deserves a bigger world." to ("మీ కళకు మరింత పెద్ద ప్రపంచం కావాలి." to "आपकी कला को एक बड़ी दुनिया मिले।"),
    "Photograph it. Tell its story. Review and share." to ("ఫోటో తీయండి. దాని కథ చెప్పండి. సమీక్షించి పంచుకోండి." to "फ़ोटो लें। इसकी कहानी बताएँ। जाँचें और साझा करें।"),
    "Show your craft. Tell its story." to ("మీ కళను చూపించండి. దాని కథ చెప్పండి." to "अपनी कला दिखाएँ। इसकी कहानी बताएँ।"),
    "Choose a clear photo of your product." to ("మీ ఉత్పత్తి స్పష్టమైన ఫోటోను ఎంచుకోండి." to "अपने उत्पाद की साफ़ फ़ोटो चुनें।"),
    "Describe the material, size and how you made it. Record up to 29 seconds." to ("పదార్థం, పరిమాణం, ఎలా చేశారో చెప్పండి. 29 సెకన్ల వరకు రికార్డ్ చేయండి." to "सामग्री, आकार और बनाने का तरीका बताएँ। अधिकतम 29 सेकंड रिकॉर्ड करें।"),
    "Both a photo and a voice recording are required." to ("ఫోటో మరియు వాయిస్ రికార్డింగ్ రెండూ అవసరం." to "फ़ोटो और आवाज़ की रिकॉर्डिंग दोनों ज़रूरी हैं।"),
    "Your photo and recording will wait safely for a connection. You will review the listing before it is published." to ("కనెక్షన్ వచ్చే వరకు మీ ఫోటో, రికార్డింగ్ సురక్షితంగా వేచి ఉంటాయి. ప్రచురించే ముందు మీరు లిస్టింగ్‌ను సమీక్షిస్తారు." to "कनेक्शन मिलने तक आपकी फ़ोटो और रिकॉर्डिंग सुरक्षित रहेंगी। प्रकाशित करने से पहले आप लिस्टिंग की समीक्षा करेंगे।"),
    "AI suggestions need your review. Check every detail before publishing." to ("AI సూచనలను సమీక్షించండి. ప్రచురించే ముందు ప్రతి వివరాన్ని తనిఖీ చేయండి." to "AI सुझाव जाँचें। प्रकाशित करने से पहले हर विवरण देखें।"),
    "Give your craft a name and a story." to ("మీ కళకు పేరు, కథ ఇవ్వండి." to "अपनी कला को नाम और कहानी दें।"),
    "Make KalaSetu comfortable for you." to ("కళాసేతును మీకు అనుకూలంగా మార్చుకోండి." to "कलासेतु को अपनी सुविधा के अनुसार बदलें।"),
    "Show my phone to buyers" to ("కొనుగోలుదారులకు నా ఫోన్ చూపించు" to "खरीदारों को मेरा फ़ोन दिखाएँ"),
    "Load more" to ("మరిన్ని చూపించు" to "और दिखाएँ"),
    "Your craft, thoughtfully collected." to ("మీ కళల సేకరణ." to "आपकी कला का संग्रह।"),
    "Add Product" to ("ఉత్పత్తిని జోడించండి" to "उत्पाद जोड़ें"), "Tap to capture product photo" to ("ఉత్పత్తి ఫోటో తీయడానికి నొక్కండి" to "उत्पाद की फ़ोटो लेने के लिए टैप करें"),
    "Recording your product story" to ("మీ ఉత్పత్తి కథను రికార్డ్ చేస్తున్నారు" to "आपके उत्पाद की कहानी रिकॉर्ड हो रही है"), "Speak about your product in your language" to ("మీ భాషలో మీ ఉత్పత్తి గురించి చెప్పండి" to "अपनी भाषा में अपने उत्पाद के बारे में बोलें"),
    "Generate listing →" to ("లిస్టింగ్ రూపొందించు →" to "लिस्टिंग तैयार करें →"),
    "Product details" to ("ఉత్పత్తి వివరాలు" to "उत्पाद विवरण"),
    "Home" to ("హోమ్" to "होम"), "Catalog" to ("కేటలాగ్" to "कैटलॉग"), "Create" to ("సృష్టించు" to "बनाएँ"),
    "Insights" to ("విశ్లేషణ" to "जानकारी"), "Profile" to ("ప్రొఫైల్" to "प्रोफ़ाइल"), "Settings" to ("సెట్టింగ్‌లు" to "सेटिंग"),
    "Drafts" to ("చిత్తుప్రతులు" to "ड्राफ़्ट"), "Published" to ("ప్రచురించినవి" to "प्रकाशित"), "Pending sync" to ("సింక్ పెండింగ్" to "सिंक बाकी"),
    "Failed" to ("విఫలమైనవి" to "विफल"), "Total products" to ("మొత్తం ఉత్పత్తులు" to "कुल उत्पाद"),
    "Create listing" to ("లిస్టింగ్ సృష్టించు" to "लिस्टिंग बनाएँ"), "Recent products" to ("ఇటీవలి ఉత్పత్తులు" to "हाल के उत्पाद"),
    "Discover" to ("కనుగొనండి" to "खोजें"), "Sync center" to ("సింక్ కేంద్రం" to "सिंक केंद्र"),
    "Search products, categories, tags" to ("ఉత్పత్తులు, వర్గాలు, ట్యాగ్‌లు వెతకండి" to "उत्पाद, श्रेणी, टैग खोजें"),
    "All" to ("అన్నీ" to "सभी"), "All categories" to ("అన్ని వర్గాలు" to "सभी श्रेणियाँ"),
    "Newest" to ("కొత్తవి" to "नवीनतम"), "Oldest" to ("పాతవి" to "सबसे पुराने"), "Recently updated" to ("ఇటీవల సవరించినవి" to "हाल में बदले"),
    "Price: low to high" to ("ధర: తక్కువ నుండి ఎక్కువ" to "कीमत: कम से अधिक"), "Price: high to low" to ("ధర: ఎక్కువ నుండి తక్కువ" to "कीमत: अधिक से कम"),
    "No products yet" to ("ఇంకా ఉత్పత్తులు లేవు" to "अभी कोई उत्पाद नहीं"), "No matching products" to ("సరిపోలే ఉత్పత్తులు లేవు" to "कोई मेल नहीं मिला"),
    "Refresh" to ("తాజాకరించు" to "रिफ़्रेश"), "Retry" to ("మళ్లీ ప్రయత్నించు" to "फिर कोशिश करें"), "Retry all" to ("అన్నీ మళ్లీ ప్రయత్నించు" to "सभी पुनः प्रयास"),
    "Edit" to ("సవరించు" to "बदलें"), "Share" to ("పంచుకోండి" to "साझा करें"), "Duplicate" to ("నకలు" to "प्रतिलिपि"),
    "Delete" to ("తొలగించు" to "हटाएँ"), "Archive" to ("భద్రపరచు" to "संग्रहित करें"), "Cancel" to ("రద్దు" to "रद्द करें"),
    "Save" to ("సేవ్" to "सहेजें"), "Save draft" to ("చిత్తుప్రతి సేవ్" to "ड्राफ़्ट सहेजें"), "Review listing" to ("లిస్టింగ్ సమీక్ష" to "लिस्टिंग जाँचें"),
    "Title" to ("శీర్షిక" to "शीर्षक"), "Description" to ("వివరణ" to "विवरण"), "Category" to ("వర్గం" to "श्रेणी"),
    "Tags" to ("ట్యాగ్‌లు" to "टैग"), "Your price (INR)" to ("మీ ధర (రూపాయలు)" to "आपकी कीमत (रुपये)"),
    "Original" to ("అసలు" to "मूल"), "Enhanced" to ("మెరుగుపరచినది" to "सुधारी गई"), "Price guidance" to ("ధర సూచన" to "कीमत सुझाव"),
    "Ready to publish" to ("ప్రచురించడానికి సిద్ధం" to "प्रकाशित करने के लिए तैयार"), "Needs attention" to ("సవరించాలి" to "ध्यान दें"),
    "Confirm and publish" to ("నిర్ధారించి ప్రచురించు" to "पुष्टि करके प्रकाशित करें"), "Read aloud" to ("చదివి వినిపించు" to "सुनें"),
    "Take photo" to ("ఫోటో తీయండి" to "फ़ोटो लें"), "Choose photo" to ("ఫోటో ఎంచుకోండి" to "फ़ोटो चुनें"),
    "Record voice" to ("మాట రికార్డ్ చేయండి" to "आवाज़ रिकॉर्ड करें"), "Stop recording" to ("రికార్డింగ్ ఆపు" to "रिकॉर्डिंग रोकें"),
    "Play recording" to ("రికార్డింగ్ వినండి" to "रिकॉर्डिंग सुनें"), "Generate listing" to ("లిస్టింగ్ రూపొందించు" to "लिस्टिंग तैयार करें"),
    "Online" to ("ఆన్‌లైన్" to "ऑनलाइन"), "Offline" to ("ఆఫ్‌లైన్" to "ऑफ़लाइन"),
    "Language" to ("భాష" to "भाषा"), "Appearance" to ("రూపం" to "दिखावट"), "Light" to ("లైట్" to "हल्का"),
    "Dark" to ("డార్క్" to "गहरा"), "System" to ("సిస్టమ్" to "सिस्टम"), "Simple view" to ("సులభ వీక్షణ" to "सरल दृश्य"),
    "Larger text" to ("పెద్ద అక్షరాలు" to "बड़ा अक्षर"), "High contrast" to ("అధిక కాంట్రాస్ట్" to "उच्च कंट्रास्ट"),
    "Voice guidance" to ("వాయిస్ మార్గదర్శకత్వం" to "आवाज़ से मार्गदर्शन"), "Notifications" to ("నోటిఫికేషన్‌లు" to "सूचनाएँ"),
    "Privacy" to ("గోప్యత" to "गोपनीयता"), "Help" to ("సహాయం" to "सहायता"), "About" to ("గురించి" to "परिचय"),
    "Display name" to ("ప్రదర్శన పేరు" to "दिखने वाला नाम"), "Shop name" to ("దుకాణం పేరు" to "दुकान का नाम"),
    "Craft specialization" to ("కళా నైపుణ్యం" to "शिल्प विशेषज्ञता"), "Location" to ("ప్రాంతం" to "स्थान"),
    "Short bio" to ("సంక్షిప్త పరిచయం" to "संक्षिप्त परिचय"), "Phone" to ("ఫోన్" to "फ़ोन"), "Email" to ("ఇమెయిల్" to "ईमेल"),
    "Contact artisan" to ("కళాకారుని సంప్రదించు" to "कारीगर से संपर्क करें"), "Favorites" to ("ఇష్టమైనవి" to "पसंदीदा"),
    "Good morning" to ("శుభోదయం" to "सुप्रभात"), "Good afternoon" to ("శుభ మధ్యాహ్నం" to "नमस्कार"), "Good evening" to ("శుభ సాయంత్రం" to "शुभ संध्या"),
    "Artisan" to ("కళాకారుడు" to "कारीगर"), "AI assistant" to ("AI సహాయకుడు" to "AI सहायक"), "Apply proposal" to ("సూచనను వర్తింపజేయి" to "सुझाव लागू करें")
)
fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply { maximumFractionDigits = 2 }.format(value)
fun dateLabel(value: Long): String = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(value))
@Composable fun SectionTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label(title), style = MaterialTheme.typography.headlineMedium)
        subtitle?.let { Text(label(it), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}
@Composable fun ProductImage(product: Product, original: Boolean = false, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = modifier) {
        SubcomposeAsyncImage(model = if (original) product.localImageUri ?: product.originalImageUrl else product.imageUrl ?: product.localImageUri ?: product.originalImageUrl,
            contentDescription = product.title.ifBlank { label("Product photo") }, contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().aspectRatio(1.2f),
            loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(label("Loading photo"), style = MaterialTheme.typography.labelSmall) } },
            error = { Box(Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) { Text(label("Photo unavailable"), style = MaterialTheme.typography.labelSmall) } })
    }
}
@Composable fun StatusLabel(product: Product) {
    SuggestionChip(onClick = {}, label = { Text(label(product.status.label)) })
}
@Composable fun ProductRow(product: Product, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ProductImage(product, modifier = Modifier.width(92.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (product.sampleDrawableRes != null) Text(label("Legacy example · excluded from insights"), style = MaterialTheme.typography.labelSmall)
                Text(product.title.ifBlank { label("Drafts") }, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Text(label(product.category.displayName), style = MaterialTheme.typography.bodySmall)
                Text(if (product.finalPrice > 0) money(product.finalPrice) else label("Price not set"), style = MaterialTheme.typography.titleMedium)
                Text("${label(product.status.label)} · ${dateLabel(product.createdAt)}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
@Composable fun ActionButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(16.dp)) { Text(label(text)) }
}
@Composable fun EmptyProducts(onCreate: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("No products yet", "Start with a photo and a short voice note about your craft.")
        ActionButton("Create listing", onCreate)
    }
}
