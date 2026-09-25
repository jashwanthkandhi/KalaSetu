import io
import logging
from typing import Dict, Any
from PIL import Image
from ..config import settings

logger = logging.getLogger("kalasetu.category")

# ImageNet class name keyword mapping to Indian Craft Categories
IMAGENET_TO_CRAFT = {
    "pot": "Pottery",
    "vase": "Pottery",
    "jar": "Pottery",
    "bowl": "Pottery",
    "pitcher": "Pottery",
    "coffeepot": "Pottery",
    "teapot": "Pottery",
    "earthenware": "Pottery",
    "wool": "Textiles",
    "scarf": "Textiles",
    "stole": "Textiles",
    "quilt": "Textiles",
    "velvet": "Textiles",
    "poncho": "Textiles",
    "sarong": "Textiles",
    "shawl": "Textiles",
    "basket": "Bamboo",
    "wicker": "Bamboo",
    "hamper": "Bamboo",
    "shopping_basket": "Bamboo",
    "wooden_spoon": "Wood",
    "crate": "Wood",
    "chest": "Wood",
    "chiffonier": "Wood",
    "necklace": "Jewellery",
    "bracelet": "Jewellery",
    "earring": "Jewellery",
    "chain": "Jewellery",
    "wallet": "Leather",
    "purse": "Leather",
    "holster": "Leather",
}


class CategoryService:
    def __init__(self):
        self.model = None
        self.transform = None
        self.categories = None
        self._load_attempted = False

    def _init_model(self):
        try:
            import torch
            import torchvision.models as models
            import torchvision.transforms as T

            weights = models.MobileNet_V3_Small_Weights.DEFAULT
            self.model = models.mobilenet_v3_small(weights=weights)
            self.model.eval()
            self.categories = weights.meta["categories"]
            self.transform = weights.transforms()
            logger.info("MobileNetV3-Small loaded successfully.")
        except Exception as e:
            logger.warning(f"Could not load MobileNetV3-Small: {e}. Fallback to heuristic.")
            self.model = None

    def predict(self, image_bytes: bytes) -> Dict[str, Any]:
        """
        Predicts craft category from image bytes.
        Returns {"name": category_name, "confidence": float}.
        Never raises exceptions to caller.
        """
        if settings.MOCK_MODE:
            return {"name": "Pottery", "confidence": 0.87}
        if not self._load_attempted:
            self._load_attempted = True
            self._init_model()
        if self.model is None:
            return {"name": "Other", "confidence": 0.0}

        try:
            import torch
            image = Image.open(io.BytesIO(image_bytes))
            if image.mode != "RGB":
                image = image.convert("RGB")

            tensor = self.transform(image).unsqueeze(0)
            with torch.no_grad():
                output = self.model(tensor)
                probabilities = torch.nn.functional.softmax(output[0], dim=0)

            top_prob, top_idx = torch.topk(probabilities, 5)
            
            # Map top predictions to craft categories
            for prob, idx in zip(top_prob.tolist(), top_idx.tolist()):
                class_name = self.categories[idx].lower()
                for keyword, craft in IMAGENET_TO_CRAFT.items():
                    if keyword in class_name:
                        confidence = round(float(prob), 2)
                        if confidence >= 0.35:
                            return {"name": craft, "confidence": confidence}

            # If top prediction has high confidence in a mapped class or fallback
            best_prob = round(float(top_prob[0]), 2)
            if best_prob < 0.4:
                return {"name": "Other", "confidence": best_prob}

            return {"name": "Other", "confidence": best_prob}
        except Exception as e:
            logger.warning(f"Category prediction error: {e}. Defaulting to 'Other'.")
            return {"name": "Other", "confidence": 0.0}


category_service = CategoryService()
