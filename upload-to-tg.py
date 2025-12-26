import os
import asyncio
from telethon import TelegramClient

# -------------------
# 配置区
# -------------------
API_ID = int(os.environ.get("API_ID"))
API_HASH = os.environ.get("API_HASH")
BOT_TOKEN = os.environ.get("BOT_TOKEN")
CHAT_ID = os.environ.get("CHAT_ID")  # @channel 或 chat_id

# GitHub Actions 传入参数
TAG = os.environ.get("TAG")
COMMIT_SHA = os.environ.get("COMMIT_SHA")
COMMIT_MESSAGE = os.environ.get("COMMIT_MESSAGE")
COMMIT_AUTHOR_NAME = os.environ.get("COMMIT_AUTHOR_NAME")
COMMIT_AUTHOR_GITHUB = os.environ.get("COMMIT_AUTHOR_GITHUB", COMMIT_AUTHOR_NAME)

# APK 文件路径
apk_paths = [
    os.environ.get("ARM64_APK"),
    os.environ.get("ARMV7_APK"),
    os.environ.get("X86_APK"),
    os.environ.get("X86_64_APK"),
    os.environ.get("UNIVERSAL_APK")
]
apk_paths = [p for p in apk_paths if p]
file_names = [os.path.basename(p) for p in apk_paths]

# -------------------
# 生成消息文本
# -------------------
def generate_message(file_names):
    file_list_text = "\n".join(file_names)
    message = f"""{file_list_text}

🎉 New Test Version Build Success! ({TAG})
Target by [{COMMIT_AUTHOR_NAME}](https://github.com/{COMMIT_AUTHOR_GITHUB})
✨ What's Changed:

{COMMIT_MESSAGE}

- arm64-v8a: 适用于64位ARM设备 (主流新手机)
- armeabi-v7a: 适用于32位ARM设备 (旧款手机)
- x86 / x86_64: 主要用于PC上的安卓模拟器
- universal: 通用版 (不确定时使用，体积较大)
参考配置文件：[魔改版-config_v2.json](https://t.me/Sesame_TK_Channel/306)
See commit detail [here](https://github.com/{COMMIT_AUTHOR_GITHUB}/Sesame-TK/commit/{COMMIT_SHA})
"""
    return message

# -------------------
# 异步上传文件
# -------------------
async def main():
    client = TelegramClient('bot_session', API_ID, API_HASH).start(bot_token=BOT_TOKEN)

    if not apk_paths:
        print("No APK files found to upload.")
        return

    # 先发送消息
    message_text = generate_message(file_names)
    await client.send_message(CHAT_ID, message_text, parse_mode='md')

    # 上传 APK 文件
    for path in apk_paths:
        print(f"Uploading {path} ...")
        await client.send_file(CHAT_ID, path)
        print(f"Uploaded {path} ✅")

    await client.disconnect()
    print("All files uploaded successfully!")

if __name__ == "__main__":
    asyncio.run(main())
