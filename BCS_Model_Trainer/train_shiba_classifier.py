import os
import cv2
import numpy as np
from sklearn.model_selection import train_test_split
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Conv2D, MaxPooling2D, Flatten, Dense, Dropout
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.utils import to_categorical

# ================== 基本設定 ==================
IMAGE_SIZE = 224
DATASET_PATH = "dataset/shiba"
NUM_CLASSES = 9
EPOCHS = 50
BATCH_SIZE = 8

images = []
labels = []

print("📥 讀取柴犬資料集")

for i in range(1, 10):
    folder = os.path.join(DATASET_PATH, f"BCS{i}")
    files = os.listdir(folder)

    print(f"BCS{i}: {len(files)} 張")

    for f in files:
        img = cv2.imread(os.path.join(folder, f))
        if img is None:
            continue
        img = cv2.resize(img, (IMAGE_SIZE, IMAGE_SIZE))
        images.append(img)
        labels.append(i - 1)  # 0~8

images = np.array(images, dtype="float32") / 255.0
labels = to_categorical(labels, NUM_CLASSES)

X_train, X_val, y_train, y_val = train_test_split(
    images, labels, test_size=0.2, random_state=42
)

# ================== 模型 ==================
model = Sequential([
    Conv2D(32, 3, activation="relu", input_shape=(224,224,3)),
    MaxPooling2D(),
    Conv2D(64, 3, activation="relu"),
    MaxPooling2D(),
    Conv2D(128, 3, activation="relu"),
    MaxPooling2D(),
    Flatten(),
    Dense(128, activation="relu"),
    Dropout(0.5),
    Dense(9, activation="softmax")
])

model.compile(
    optimizer="adam",
    loss="categorical_crossentropy",
    metrics=["accuracy"]
)

print("🚀 開始訓練柴犬 BCS 分類模型")

model.fit(
    X_train, y_train,
    epochs=EPOCHS,
    validation_data=(X_val, y_val)
)

model.save("bcs_shiba_classifier.h5")
print("✅ 已儲存：bcs_shiba_classifier.h5")
