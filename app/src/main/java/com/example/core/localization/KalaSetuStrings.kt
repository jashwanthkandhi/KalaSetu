package com.example.core.localization

import com.example.core.model.AppLanguage

object KalaSetuStrings {

    fun appTagline(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "మీ కళకు మరింత పెద్ద ప్రపంచం లభించాలి."
        AppLanguage.HINDI -> "आपकी कला को एक बड़ी दुनिया मिलनी चाहिए।"
        AppLanguage.ENGLISH -> "Your craft deserves a bigger world."
    }

    fun onboardingSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "సృష్టించండి. పంచుకోండి. ఎదగండి. భారతీయ కళాకారుల కోసం AI ఆధారిత మార్కెట్‌ప్లేస్."
        AppLanguage.HINDI -> "सृजन करें। साझा करें। आगे बढ़ें। भारतीय कारीगरों के लिए AI-संचालित मंच।"
        AppLanguage.ENGLISH -> "Create. Share. Grow. AI-powered digital marketplace manager for Indian artisans."
    }

    fun startCreating(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "సృష్టించడం ప్రారంభించండి"
        AppLanguage.HINDI -> "शुरू करें"
        AppLanguage.ENGLISH -> "Start Creating"
    }

    fun skip(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "దాటవేయి"
        AppLanguage.HINDI -> "छोड़ें"
        AppLanguage.ENGLISH -> "Skip"
    }

    // Capture Screen
    fun addProduct(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ఉత్పత్తిని జోడించండి"
        AppLanguage.HINDI -> "उत्पाद जोड़ें"
        AppLanguage.ENGLISH -> "Add Product"
    }

    fun tapToCapture(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ఉత్పత్తి ఫోటోను తీయండి లేదా ఎంచుకోండి"
        AppLanguage.HINDI -> "उत्पाद की फोटो लें या चुनें"
        AppLanguage.ENGLISH -> "Tap to capture product photo"
    }

    fun retakePhoto(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "మరో ఫోటో తీయండి"
        AppLanguage.HINDI -> "दोबारा फोटो लें"
        AppLanguage.ENGLISH -> "Retake Photo"
    }

    fun chooseFromGallery(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "గ్యాలరీ నుండి"
        AppLanguage.HINDI -> "गैलरी से चुनें"
        AppLanguage.ENGLISH -> "From Gallery"
    }

    fun takeCameraPhoto(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "కెమెరాతో తీయండి"
        AppLanguage.HINDI -> "कैमरा से लें"
        AppLanguage.ENGLISH -> "Take Photo"
    }

    fun speakAboutProduct(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "మీ భాషలో మీ ఉత్పత్తి గురించి మాట్లాడండి"
        AppLanguage.HINDI -> "अपनी भाषा में अपने उत्पाद के बारे में बोलें"
        AppLanguage.ENGLISH -> "Speak about your product in your language"
    }

    fun tapToRecord(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "రికార్డ్ చేయడానికి నొక్కండి"
        AppLanguage.HINDI -> "रिकॉर्ड करने के लिए टैप करें"
        AppLanguage.ENGLISH -> "Tap to Record"
    }

    fun recording(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "రికార్డింగ్ అవుతోంది..."
        AppLanguage.HINDI -> "रिकॉर्डिंग जारी है..."
        AppLanguage.ENGLISH -> "Recording in progress..."
    }

    fun stopRecording(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "రికార్డింగ్ ఆపండి"
        AppLanguage.HINDI -> "रिकॉर्डिंग रोकें"
        AppLanguage.ENGLISH -> "Stop Recording"
    }

    fun rerecord(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "మళ్లీ రికార్డ్ చేయండి"
        AppLanguage.HINDI -> "पुनः रिकॉर्ड करें"
        AppLanguage.ENGLISH -> "Re-record"
    }

    fun playPreview(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "వినండి"
        AppLanguage.HINDI -> "सुनें"
        AppLanguage.ENGLISH -> "Play Voice"
    }

    fun offlineBanner(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "మీరు ఆఫ్‌లైన్‌లో ఉన్నారు — కనెక్ట్ అయినప్పుడు మీ లిస్టింగ్ అప్‌లోడ్ అవుతుంది."
        AppLanguage.HINDI -> "आप ऑफ़लाइन हैं — इंटरनेट कनेक्ट होने पर आपकी लिस्टिंग अपलोड होगी।"
        AppLanguage.ENGLISH -> "You're offline — your listing will upload when you're connected."
    }

    fun generateListing(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "లిస్టింగ్ రూపొందించండి"
        AppLanguage.HINDI -> "लिस्टिंग बनाएं"
        AppLanguage.ENGLISH -> "Generate Listing"
    }

    fun generateListingHelper(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ఫోటో మరియు వాయిస్ నుండి AI లిస్టింగ్‌ను సృష్టిస్తుంది"
        AppLanguage.HINDI -> "फोटो और आवाज़ से AI लिस्टिंग तैयार करेगा"
        AppLanguage.ENGLISH -> "We'll create your listing from photo and voice"
    }

    fun needPhotoAndAudio(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ముందుగా ఫోటో తీసి వాయిస్ రికార్డ్ చేయండి"
        AppLanguage.HINDI -> "कृपया पहले फोटो लें और आवाज़ रिकॉर्ड करें"
        AppLanguage.ENGLISH -> "Please add both a photo and voice recording"
    }

    // Processing Screen
    fun creatingYourListing(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "మీ లిస్టింగ్‌ను సృష్టిస్తోంది..."
        AppLanguage.HINDI -> "आपकी लिस्टिंग तैयार हो रही है..."
        AppLanguage.ENGLISH -> "Creating your listing..."
    }

    fun stepTranscribing(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "మీ స్వరాన్ని అర్థం చేసుకుంటోంది (Transcribing)"
        AppLanguage.HINDI -> "आपकी आवाज़ को समझा जा रहा है (Transcribing)"
        AppLanguage.ENGLISH -> "Understanding your voice"
    }

    fun stepEnhancing(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ఉత్పత్తి ఫోటోను మెరుగుపరుస్తోంది (Enhancing photo)"
        AppLanguage.HINDI -> "फोटो को बेहतर बनाया जा रहा है (Enhancing photo)"
        AppLanguage.ENGLISH -> "Enhancing your product photo"
    }

    fun stepCategorising(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "కళారూపాన్ని గుర్తిస్తోంది (Identifying craft)"
        AppLanguage.HINDI -> "हस्तशिल्प श्रेणी की पहचान (Identifying craft)"
        AppLanguage.ENGLISH -> "Identifying your craft"
    }

    fun stepGenerating(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "పూర్తి లిస్టింగ్‌ను సిద్ధం చేస్తోంది (Generating listing)"
        AppLanguage.HINDI -> "विवरण और मूल्य तैयार किया जा रहा है"
        AppLanguage.ENGLISH -> "Creating your listing"
    }

    fun takesAbout20Seconds(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "దీనికి సాధారణంగా దాదాపు 15-20 సెకన్లు పడుతుంది"
        AppLanguage.HINDI -> "इसमें आमतौर पर लगभग 15-20 सेकंड लगते हैं"
        AppLanguage.ENGLISH -> "This usually takes about 15–20 seconds"
    }

    fun somethingWentWrong(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ఏదో పొరపాటు జరిగింది."
        AppLanguage.HINDI -> "कुछ गलत हो गया।"
        AppLanguage.ENGLISH -> "Something went wrong."
    }

    fun tryAgain(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "మళ్ళీ ప్రయత్నించండి"
        AppLanguage.HINDI -> "पुनः प्रयास करें"
        AppLanguage.ENGLISH -> "Try Again"
    }

    // Review & Edit Screen
    fun reviewListing(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "లిస్టింగ్ సమీక్షించండి"
        AppLanguage.HINDI -> "लिस्टिंग की समीक्षा करें"
        AppLanguage.ENGLISH -> "Review Listing"
    }

    fun aiGeneratedBanner(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "భద్రపరిచే ముందు సమీక్షించి సవరించండి — AI ఈ లిస్టింగ్‌ను రూపొందించింది"
        AppLanguage.HINDI -> "सहेजने से पहले जांचें व सुधारें — AI ने यह लिस्टिंग बनाई है"
        AppLanguage.ENGLISH -> "Review and edit before saving — AI generated this listing"
    }

    fun aiGeneratedChip(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "AI రూపొందించినది"
        AppLanguage.HINDI -> "AI जनरेटेड"
        AppLanguage.ENGLISH -> "AI Generated"
    }

    fun titleLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "శీర్షిక (TITLE)"
        AppLanguage.HINDI -> "शीर्षक (TITLE)"
        AppLanguage.ENGLISH -> "TITLE"
    }

    fun descriptionLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "వివరణ (DESCRIPTION)"
        AppLanguage.HINDI -> "विवरण (DESCRIPTION)"
        AppLanguage.ENGLISH -> "DESCRIPTION"
    }

    fun categoryLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "వర్గం (CATEGORY)"
        AppLanguage.HINDI -> "श्रेणी (CATEGORY)"
        AppLanguage.ENGLISH -> "CATEGORY"
    }

    fun aiSuggestedPrice(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "AI సూచించిన ధర"
        AppLanguage.HINDI -> "AI अनुशंसित मूल्य"
        AppLanguage.ENGLISH -> "AI SUGGESTED"
    }

    fun yourPrice(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "మీ ధర (YOUR PRICE)"
        AppLanguage.HINDI -> "आपका मूल्य (YOUR PRICE)"
        AppLanguage.ENGLISH -> "YOUR PRICE"
    }

    fun tagsLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ట్యాగ్‌లు (TAGS)"
        AppLanguage.HINDI -> "टैग्स (TAGS)"
        AppLanguage.ENGLISH -> "TAGS"
    }

    fun originalVoiceNote(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "🎙 అసలు వాయిస్ నోట్ చూడండి"
        AppLanguage.HINDI -> "🎙 मूल वॉयस नोट देखें"
        AppLanguage.ENGLISH -> "🎙 View original voice note"
    }

    fun readAloud(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "🔊 బిగ్గరగా చదవండి"
        AppLanguage.HINDI -> "🔊 बोलकर सुनाएं"
        AppLanguage.ENGLISH -> "🔊 Read Aloud"
    }

    fun confirmListing(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "లిస్టింగ్‌ను నిర్ధారించండి"
        AppLanguage.HINDI -> "लिस्टिंग पक्की करें"
        AppLanguage.ENGLISH -> "Confirm Listing"
    }

    fun artisanControlFootnote(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "మీ చేతివృత్తి, మీ నిర్ణయం"
        AppLanguage.HINDI -> "आपका शिल्प, आपका निर्णय"
        AppLanguage.ENGLISH -> "Your craft, your decision"
    }

    // Step & Field Aliases
    fun stepVoice(lang: AppLanguage): String = stepTranscribing(lang)
    fun stepPhoto(lang: AppLanguage): String = stepEnhancing(lang)
    fun stepCraft(lang: AppLanguage): String = stepCategorising(lang)
    fun stepListing(lang: AppLanguage): String = stepGenerating(lang)
    fun processingDuration(lang: AppLanguage): String = takesAbout20Seconds(lang)
    fun title(lang: AppLanguage): String = titleLabel(lang)
    fun description(lang: AppLanguage): String = descriptionLabel(lang)
    fun category(lang: AppLanguage): String = categoryLabel(lang)
    fun tags(lang: AppLanguage): String = tagsLabel(lang)
    fun aiSuggested(lang: AppLanguage): String = aiGeneratedChip(lang)

    // Catalog Screen
    fun myCatalog(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "నా కేటలాగ్"
        AppLanguage.HINDI -> "मेरा कैटलॉग"
        AppLanguage.ENGLISH -> "My Catalog"
    }

    fun catalogSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "మీ చేతితో చేసిన ఉత్పత్తుల సమాహారం"
        AppLanguage.HINDI -> "आपका हस्तनिर्मित संग्रह"
        AppLanguage.ENGLISH -> "Your handcrafted collection"
    }

    fun emptyCatalogTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "మీ కేటలాగ్ ఖాళీగా ఉంది"
        AppLanguage.HINDI -> "आपका कैटलॉग खाली है"
        AppLanguage.ENGLISH -> "Your catalog is empty"
    }

    fun emptyCatalogSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "మీ మొదటి లిస్టింగ్‌ను సృష్టించి అమ్మకం ప్రారంభించండి."
        AppLanguage.HINDI -> "अपनी पहली लिस्टिंग बनाएं और बेचना शुरू करें।"
        AppLanguage.ENGLISH -> "Create your first listing and start sharing your craft."
    }

    fun createListing(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "+ కొత్త లిస్టింగ్ సృష్టించండి"
        AppLanguage.HINDI -> "+ नई लिस्टिंग बनाएं"
        AppLanguage.ENGLISH -> "+ Create Listing"
    }

    fun newListingCard(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "కొత్త లిస్టింగ్"
        AppLanguage.HINDI -> "नई लिस्टिंग"
        AppLanguage.ENGLISH -> "New Listing"
    }

    fun pendingUpload(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "అప్‌లోడ్ పెండింగ్‌లో ఉంది"
        AppLanguage.HINDI -> "अपलोड लंबित"
        AppLanguage.ENGLISH -> "Pending upload"
    }

    fun savedStatus(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "భద్రపరచబడింది"
        AppLanguage.HINDI -> "सहेजा गया"
        AppLanguage.ENGLISH -> "Saved"
    }

    // Bottom Navigation
    fun navCapture(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "సృష్టించండి"
        AppLanguage.HINDI -> "बनाएं"
        AppLanguage.ENGLISH -> "Capture"
    }

    fun navCatalog(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "కేటలాగ్"
        AppLanguage.HINDI -> "कैटलॉग"
        AppLanguage.ENGLISH -> "Catalog"
    }

    fun navSettings(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "సెట్టింగ్‌లు"
        AppLanguage.HINDI -> "सेटिंग्स"
        AppLanguage.ENGLISH -> "Settings"
    }

    fun onlineStatus(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ఆన్‌లైన్"
        AppLanguage.HINDI -> "ऑनलाइन"
        AppLanguage.ENGLISH -> "Online"
    }

    fun offlineStatus(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ఆఫ్‌లైన్"
        AppLanguage.HINDI -> "ऑफ़लाइन"
        AppLanguage.ENGLISH -> "Offline"
    }

    // Deletion & Confirmation Strings
    fun deleteProduct(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ఉత్పత్తిని తొలగించండి"
        AppLanguage.HINDI -> "उत्पाद हटाएं"
        AppLanguage.ENGLISH -> "Delete Product"
    }

    fun deleteConfirmationTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ఉత్పత్తిని తొలగించాలా?"
        AppLanguage.HINDI -> "क्या आप इस उत्पाद को हटाना चाहते हैं?"
        AppLanguage.ENGLISH -> "Delete this product?"
    }

    fun deleteConfirmationBody(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ఈ ఉత్పత్తి మీ కేటలాగ్ నుండి శాశ్వతంగా తొలగించబడుతుంది."
        AppLanguage.HINDI -> "यह उत्पाद आपके कैटलॉग से स्थायी रूप से हटा दिया जाएगा।"
        AppLanguage.ENGLISH -> "This product will be permanently removed from your catalog."
    }

    fun delete(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "తొలగించు"
        AppLanguage.HINDI -> "हटाएं"
        AppLanguage.ENGLISH -> "Delete"
    }

    fun cancel(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "రద్దు చేయి"
        AppLanguage.HINDI -> "रद्द करें"
        AppLanguage.ENGLISH -> "Cancel"
    }

    fun save(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "భద్రపరచు"
        AppLanguage.HINDI -> "सहेजें"
        AppLanguage.ENGLISH -> "Save"
    }

    fun edit(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "సవరించు"
        AppLanguage.HINDI -> "संपादित करें"
        AppLanguage.ENGLISH -> "Edit"
    }

    fun close(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "మూసివేయి"
        AppLanguage.HINDI -> "बंद करें"
        AppLanguage.ENGLISH -> "Close"
    }

    fun saveListing(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "లిస్టింగ్‌ను భద్రపరచండి"
        AppLanguage.HINDI -> "लिस्टिंग सहेजें"
        AppLanguage.ENGLISH -> "Save Listing"
    }

    // Settings & Extra Unique Features
    fun artisanSettings(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "కళాకారుడి సెట్టింగ్‌లు"
        AppLanguage.HINDI -> "कारीगर सेटिंग्स"
        AppLanguage.ENGLISH -> "Artisan Settings"
    }

    fun preferredLanguage(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "యాప్ భాషను ఎంచుకోండి"
        AppLanguage.HINDI -> "ऐप की भाषा चुनें"
        AppLanguage.ENGLISH -> "Choose App Language"
    }

    fun connectivityStatus(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "కనెక్టివిటీ స్థితి"
        AppLanguage.HINDI -> "कनेक्टिविटी स्थिति"
        AppLanguage.ENGLISH -> "Connectivity Status"
    }

    fun voiceGuideTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "వాయిస్ గైడ్ అసిస్టెంట్"
        AppLanguage.HINDI -> "आवाज़ सहायता (Voice Guide)"
        AppLanguage.ENGLISH -> "Voice Guidance Assistant"
    }

    fun voiceGuideSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ప్రతి స్క్రీన్‌పై చర్యలను మరియు సూచనలను బిగ్గరగా వినిపిస్తుంది"
        AppLanguage.HINDI -> "हर स्क्रीन पर निर्देशों और कार्यों को बोलकर सुनाता है"
        AppLanguage.ENGLISH -> "Speaks aloud screen actions and instructions for easy usage"
    }

    fun fairPriceTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "సరసమైన ధర కాలిక్యులేటర్"
        AppLanguage.HINDI -> "उचित मूल्य कैलकुलेटर (Fair Price)"
        AppLanguage.ENGLISH -> "Fair Craft Price Calculator"
    }

    fun fairPriceSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ముడిసరుకులు మరియు శ్రమ సమయం ఆధారంగా న్యాయమైన ధర లెక్కించండి"
        AppLanguage.HINDI -> "कच्चे माल और काम के घंटों से सही कीमत जानें"
        AppLanguage.ENGLISH -> "Calculate fair price based on raw materials and artisan craft hours"
    }

    fun shareCatalogTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "WhatsApp కేటలాగ్ షేర్ చేయండి"
        AppLanguage.HINDI -> "WhatsApp पर कैटलॉग साझा करें"
        AppLanguage.ENGLISH -> "Share WhatsApp Craft Card"
    }

    fun shareCatalogSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "మీ చేతితో చేసిన ఉత్పత్తులను కస్టమర్లకు నేరుగా పంపండి"
        AppLanguage.HINDI -> "अपने हस्तशिल्प संग्रह को ग्राहकों को तुरंत भेजें"
        AppLanguage.ENGLISH -> "Share your handcrafted collection directly with buyers"
    }

    fun artisanStoryTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "వారసత్వ కళా కథనం"
        AppLanguage.HINDI -> "कला परंपरा की कहानी (Artisan Story)"
        AppLanguage.ENGLISH -> "Artisan Heritage Story"
    }

    fun artisanStorySubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "మీ కళా ప్రయాణం మరియు చరిత్ర గురించిన ఆడియో రికార్డింగ్"
        AppLanguage.HINDI -> "अपनी पीढ़ीगत कला और कौशल की कहानी रिकॉर्ड करें"
        AppLanguage.ENGLISH -> "Record and share your generational craft journey"
    }

    fun offlineSyncTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ఆఫ్‌లైన్ సమకాలీకరణ"
        AppLanguage.HINDI -> "ऑफ़लाइन सिंक प्रबंधक"
        AppLanguage.ENGLISH -> "Offline Sync Manager"
    }

    fun offlineSyncSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ఇంటర్నెట్ అందుబాటులో ఉన్నప్పుడు ఆఫ్‌లైన్ లిస్టింగ్‌లు అప్‌లోడ్ అవుతాయి"
        AppLanguage.HINDI -> "इंटरनेट कनेक्ट होने पर ऑफ़लाइन लिस्टिंग अपलोड होंगी"
        AppLanguage.ENGLISH -> "Upload pending offline artisan listings to digital marketplace"
    }

    fun artisanProfileTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "కళాకారుడి ప్రొఫైల్"
        AppLanguage.HINDI -> "कारीगर प्रोफ़ाइल"
        AppLanguage.ENGLISH -> "Artisan Profile"
    }

    fun rawMaterialCost(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ముడిసరుకు ఖర్చు (₹)"
        AppLanguage.HINDI -> "कच्चे माल की लागत (₹)"
        AppLanguage.ENGLISH -> "Raw Material Cost (₹)"
    }

    fun craftHours(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "పని గంటలు (శ్రమ)"
        AppLanguage.HINDI -> "काम के घंटे (मेहनत)"
        AppLanguage.ENGLISH -> "Crafting Hours (Labor)"
    }

    fun recommendedFairPrice(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "సిఫార్సు చేయబడిన సరసమైన కనీస ధర:"
        AppLanguage.HINDI -> "अनुशंसित उचित न्यूनतम मूल्य:"
        AppLanguage.ENGLISH -> "Recommended Fair Minimum Price:"
    }

    fun fairPricingNote(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ఈ ధర మీ శ్రమకు న్యాయమైన వేతనం మరియు కళా గౌరవాన్ని నిర్ధారిస్తుంది."
        AppLanguage.HINDI -> "यह मूल्य आपकी मेहनत का उचित पारिश्रमिक और कला का सम्मान सुनिश्चित करता है।"
        AppLanguage.ENGLISH -> "This ensures fair daily wages for your craftsmanship without undervaluing."
    }

    fun shareCatalogMessage(artisanName: String, count: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "నమస్తే! నేను $artisanName. KalaSetu యాప్‌లో నా హస్తకళల కేటలాగ్ చూడండి ($count కళాఖండాలు సిద్ధంగా ఉన్నాయి). ప్రత్యక్ష ఆర్డర్ల కోసం సంప్రదించండి!"
        AppLanguage.HINDI -> "नमस्ते! मैं $artisanName हूँ। KalaSetu पर मेरा हस्तशिल्प कैटलॉग देखें ($count उत्पाद उपलब्ध हैं)। सीधे ऑर्डर के लिए संपर्क करें!"
        AppLanguage.ENGLISH -> "Namaste! I am $artisanName. Browse my handcrafted artisan catalog on KalaSetu ($count authentic craft items available). Connect with me for direct orders!"
    }

    fun listeningTapToStop(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "వింటోంది... ఆపడానికి నొక్కండి"
        AppLanguage.HINDI -> "सुन रहा है... रोकने के लिए टैप करें"
        AppLanguage.ENGLISH -> "Listening... Tap to stop"
    }

    fun tapToRecordVoice(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ఉత్పత్తి వివరాలు రికార్డ్ చేయడానికి నొక్కండి"
        AppLanguage.HINDI -> "उत्पाद का विवरण बोलने के लिए टैप करें"
        AppLanguage.ENGLISH -> "Tap to record description"
    }

    fun aiEnhancingPhoto(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "AI మీ ఫోటోను మెరుగుపరుస్తోంది..."
        AppLanguage.HINDI -> "AI आपकी फोटो को बेहतर बना रहा है..."
        AppLanguage.ENGLISH -> "AI is enhancing your photo..."
    }

    fun priceLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ధర (PRICE)"
        AppLanguage.HINDI -> "मूल्य (PRICE)"
        AppLanguage.ENGLISH -> "PRICE"
    }

    fun aiEstimateLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "AI అంచనా"
        AppLanguage.HINDI -> "AI अनुमान"
        AppLanguage.ENGLISH -> "AI ESTIMATE"
    }

    fun localizedCategory(category: String, lang: AppLanguage): String = when (category.lowercase()) {
        "pottery" -> when (lang) {
            AppLanguage.TELUGU -> "కుండల కళ"
            AppLanguage.HINDI -> "मिट्टी के बर्तन"
            AppLanguage.ENGLISH -> "Pottery"
        }
        "textiles" -> when (lang) {
            AppLanguage.TELUGU -> "చేనేత వస్త్రాలు"
            AppLanguage.HINDI -> "हथकरघा वस्त्र"
            AppLanguage.ENGLISH -> "Textiles"
        }
        "bamboo" -> when (lang) {
            AppLanguage.TELUGU -> "వెదురు బుట్టలు"
            AppLanguage.HINDI -> "बांस शिल्प"
            AppLanguage.ENGLISH -> "Bamboo Craft"
        }
        "wood" -> when (lang) {
            AppLanguage.TELUGU -> "చెక్క చెక్కడాలు"
            AppLanguage.HINDI -> "काष्ठ शिल्प"
            AppLanguage.ENGLISH -> "Wood Craft"
        }
        "home decor" -> when (lang) {
            AppLanguage.TELUGU -> "ఇంటి అలంకరణ"
            AppLanguage.HINDI -> "गृह सज्जा"
            AppLanguage.ENGLISH -> "Home Decor"
        }
        "jewellery" -> when (lang) {
            AppLanguage.TELUGU -> "హస్తకళా ఆభరణాలు"
            AppLanguage.HINDI -> "हस्तनिर्मित आभूषण"
            AppLanguage.ENGLISH -> "Jewellery"
        }
        "paintings" -> when (lang) {
            AppLanguage.TELUGU -> "సంప్రదాయ చిత్రలేఖనం"
            AppLanguage.HINDI -> "पारंपरिक चित्रकला"
            AppLanguage.ENGLISH -> "Paintings"
        }
        "leather" -> when (lang) {
            AppLanguage.TELUGU -> "చర్మ హస్తకళ"
            AppLanguage.HINDI -> "चर्म शिल्प"
            AppLanguage.ENGLISH -> "Leather Craft"
        }
        else -> when (lang) {
            AppLanguage.TELUGU -> "హస్తకళ"
            AppLanguage.HINDI -> "हस्तशिल्प"
            AppLanguage.ENGLISH -> "Craft"
        }
    }
}
