"""Mock data fixtures for testing and MOCK_MODE execution."""

MOCK_TRANSCRIPTS = {
    "te": "ఇది మా చేతితో చేసిన సాంప్రదాయ మట్టి కుండ, పూల నగిషీలతో కూడిన సహజమైన క్లే వాజ్.",
    "hi": "यह शुद्ध प्राकृतिक मिट्टी से बनी पारंपरिक हस्तनिर्मित सुराही है, जिस पर सुंदर नक्काशी है।",
    "en": "This is a traditional handmade terracotta vase crafted from pure river clay with hand-carved floral motifs.",
}

MOCK_LISTINGS = {
    "te": {
        "title": "హ్యాండ్‌క్రాఫ్టెడ్ టెర్రకోట వాటర్ పాట్",
        "description": "రోజువారీ ఉపయోగం కోసం తయారు చేసిన సాంప్రదాయ చేతితో చేసిన మట్టి కుండ. సహజ బంకమట్టి, పర్యావరణ అనుకూలం.",
        "category": "Pottery",
        "tags": ["terracotta", "handmade", "pottery", "kitchen"],
        "suggested_price": 450.0,
    },
    "hi": {
        "title": "हस्तनिर्मित टेराकोटा वाटर पॉट",
        "description": "दैनिक उपयोग के लिए पारंपरिक हस्तनिर्मित मिट्टी का बर्तन। प्राकृतिक मिट्टी, पर्यावरण अनुकूल।",
        "category": "Pottery",
        "tags": ["terracotta", "handmade", "pottery", "kitchen"],
        "suggested_price": 450.0,
    },
    "en": {
        "title": "Handcrafted Terracotta Water Pot",
        "description": "Traditional handmade terracotta pot for everyday water storage. Natural river clay, eco-friendly.",
        "category": "Pottery",
        "tags": ["terracotta", "handmade", "pottery", "kitchen"],
        "suggested_price": 450.0,
    },
}
