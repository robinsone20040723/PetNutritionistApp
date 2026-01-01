import os
from collections import defaultdict

DATASET_DIR = "dataset_bcs"

bcs_total_count = {}
bcs_breed_count = {}

for bcs in sorted(os.listdir(DATASET_DIR)):
    bcs_path = os.path.join(DATASET_DIR, bcs)
    if not os.path.isdir(bcs_path):
        continue

    total = 0
    breed_counter = defaultdict(int)

    for img in os.listdir(bcs_path):
        if not img.lower().endswith((".jpg", ".png", ".jpeg")):
            continue

        total += 1

        # 檔名格式：breed_xxx.jpg
        breed = img.split("_")[0]
        breed_counter[breed] += 1

    bcs_total_count[bcs] = total
    bcs_breed_count[bcs] = breed_counter

# ===== 印出結果 =====
print("\n📊 BCS 資料集統計結果\n")

for bcs in sorted(bcs_total_count.keys()):
    print(f"🔹 {bcs.upper()}：{bcs_total_count[bcs]} 張")

    for breed, count in sorted(
        bcs_breed_count[bcs].items(), key=lambda x: -x[1]
    ):
        print(f"    - {breed}: {count}")

    print("-" * 30)

print("✅ 分析完成")
