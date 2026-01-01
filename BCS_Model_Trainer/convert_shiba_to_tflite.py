import tensorflow as tf
import os

# ===== 模型路徑 =====
MODEL_PATH = "bcs_shiba_classifier.h5"
TFLITE_PATH = "bcs_shiba_classifier.tflite"

print("🔥 Loading model:", os.path.abspath(MODEL_PATH))

# ⭐ 關鍵：compile=False
model = tf.keras.models.load_model(MODEL_PATH, compile=False)
model.summary()

# ===== TFLite 轉換 =====
converter = tf.lite.TFLiteConverter.from_keras_model(model)

# ⭐ 不量化，確保分類機率正常
converter.optimizations = []

# ⭐ Android 安全 ops
converter.target_spec.supported_ops = [
    tf.lite.OpsSet.TFLITE_BUILTINS
]

# ⭐ 強制 float32（超重要）
converter.inference_input_type = tf.float32
converter.inference_output_type = tf.float32

print("🔄 Converting to TFLite...")
tflite_model = converter.convert()

with open(TFLITE_PATH, "wb") as f:
    f.write(tflite_model)

print("✅ TFLite saved:", TFLITE_PATH)
print("📦 Size:", len(tflite_model) / 1024, "KB")
