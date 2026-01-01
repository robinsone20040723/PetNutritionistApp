import cv2
import numpy as np
import tensorflow as tf

IMAGE_SIZE = 224
MODEL_PATH = "bcs_shiba_classifier.h5"   # ⭐ 用柴犬模型
IMAGE_PATH = "test_images/1.jpg"

# 載入模型
model = tf.keras.models.load_model(MODEL_PATH, compile=False)

# 讀取圖片
img = cv2.imread(IMAGE_PATH)
if img is None:
    raise ValueError("❌ 圖片讀取失敗")

img = cv2.resize(img, (IMAGE_SIZE, IMAGE_SIZE))
img = img.astype("float32") / 255.0
img = np.expand_dims(img, axis=0)

# 預測
probs = model.predict(img)[0]   # shape = (9,)
bcs = np.argmax(probs) + 1      # 0~8 → 1~9

print("🔍 BCS probabilities:", probs)
print("🐕 Predicted BCS:", bcs)
