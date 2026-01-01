import os
import cv2
import numpy as np
from sklearn.model_selection import train_test_split
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Conv2D, MaxPooling2D, Flatten, Dense, Dropout
from tensorflow.keras.preprocessing.image import ImageDataGenerator

print("✅ 程式開始執行")
print("📂 目前工作目錄:", os.getcwd())

IMAGE_SIZE = 224
DATASET_PATH = "dataset_bcs"   # 裡面是 BCS1~BCS9
EPOCHS = 50                    # ⭐先拉高一點
BATCH_SIZE = 8

images = []
labels = []

print("📥 開始讀取資料集...")

for i in range(1, 10):
    folder = os.path.join(DATASET_PATH, f"BCS{i}")
    if not os.path.exists(folder):
        print(f"❌ 找不到資料夾: {folder}")
        continue

    files = os.listdir(folder)
    print(f"➡️ BCS{i} 圖片數量: {len(files)}")

    for file in files:
        img_path = os.path.join(folder, file)
        img = cv2.imread(img_path)
        if img is None:
            continue

        img = cv2.resize(img, (IMAGE_SIZE, IMAGE_SIZE))
        images.append(img)
        labels.append(float(i))   # ⭐回歸標籤 1~9

print("📊 總讀取圖片數:", len(images))
if len(images) == 0:
    print("❌ 沒有圖片，訓練中止")
    exit()

X = np.array(images, dtype="float32") / 255.0
y = np.array(labels, dtype="float32")

X_train, X_val, y_train, y_val = train_test_split(
    X, y, test_size=0.2, random_state=42
)

train_datagen = ImageDataGenerator(
    rotation_range=25,
    width_shift_range=0.1,
    height_shift_range=0.1,
    zoom_range=0.2,
    horizontal_flip=True
)

train_generator = train_datagen.flow(X_train, y_train, batch_size=BATCH_SIZE)

print("🧠 建立回歸模型")

model = Sequential([
    Conv2D(32, (3,3), activation='relu', input_shape=(IMAGE_SIZE, IMAGE_SIZE, 3)),
    MaxPooling2D(2,2),

    Conv2D(64, (3,3), activation='relu'),
    MaxPooling2D(2,2),

    Conv2D(128, (3,3), activation='relu'),
    MaxPooling2D(2,2),

    Flatten(),
    Dense(128, activation='relu'),
    Dropout(0.5),

    Dense(1, activation='linear')  # ⭐回歸輸出
])

model.compile(optimizer='adam', loss='mse', metrics=['mae'])

print("🚀 開始訓練（Regression）")
model.fit(train_generator, epochs=EPOCHS, validation_data=(X_val, y_val))

MODEL_NAME = "bcs_regression_model.h5"
model.save(MODEL_NAME)
print(f"🎉 訓練完成，模型已儲存：{MODEL_NAME}")
