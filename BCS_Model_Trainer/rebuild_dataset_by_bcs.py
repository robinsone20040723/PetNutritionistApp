import os
import shutil

SRC_ROOT = "dataset"
DST_ROOT = "dataset_bcs"

os.makedirs(DST_ROOT, exist_ok=True)

for breed in os.listdir(SRC_ROOT):
    breed_path = os.path.join(SRC_ROOT, breed)
    if not os.path.isdir(breed_path):
        continue

    for bcs in os.listdir(breed_path):
        src_bcs_path = os.path.join(breed_path, bcs)
        if not os.path.isdir(src_bcs_path):
            continue

        dst_bcs_path = os.path.join(DST_ROOT, bcs)
        os.makedirs(dst_bcs_path, exist_ok=True)

        for img in os.listdir(src_bcs_path):
            src_img = os.path.join(src_bcs_path, img)
            new_name = f"{breed}_{img}"
            dst_img = os.path.join(dst_bcs_path, new_name)

            if not os.path.exists(dst_img):
                shutil.copy(src_img, dst_img)

print("✅ dataset_bcs 建立完成（含柴犬）")
