import os
import asyncio
from telethon import TelegramClient

# -------------------
# 配置区
# -------------------
# 转换 API_ID 为整数
try:
    API_ID = int(os.environ.get("API_ID"))
except (ValueError, TypeError):
    print("Error: API_ID is missing or invalid.")
    exit(1)

API_HASH = os.environ.get("API_HASH")
BOT_TOKEN = os.environ.get("BOT_TOKEN")

# 处理 CHAT_ID (支持数字ID和@username)
chat_id_raw = os.environ.get("CHAT_ID")
try:
    CHAT_ID = int(chat_id_raw)
except (ValueError, TypeError):
    CHAT_ID = chat_id_raw

# -------------------
# Git 信息处理 (增加默认值防止空指针)
# -------------------
TAG = os.environ.get("TAG", "Unknown Tag")
COMMIT_SHA = os.environ.get("COMMIT_SHA", "000000")
COMMIT_MESSAGE = os.environ.get("COMMIT_MESSAGE", "")
# 如果 YAML 没传下来，默认显示 Unknown
COMMIT_AUTHOR_NAME = os.environ.get("COMMIT_AUTHOR_NAME", "Unknown Author")
COMMIT_AUTHOR_GITHUB = os.environ.get("COMMIT_AUTHOR_GITHUB", COMMIT_AUTHOR_NAME)

# 如果 COMMIT_MESSAGE 依然为空（例如 Tag 触发），给一个默认文案
if not COMMIT_MESSAGE:
    COMMIT_MESSAGE = f"No commit message details (Triggered by {TAG})"

# -------------------
# APK 文件路径处理
# -------------------
apk_paths = [
    os.environ.get("ARM64_APK"),
    os.environ.get("ARMV7_APK"),
    os.environ.get("X86_APK"),
    os.environ.get("X86_64_APK"),
    os.environ.get("UNIVERSAL_APK")
]
# 过滤掉空路径和不存在的文件
valid_apks = [p for p in apk_paths if p and os.path.exists(p)]

# -------------------
# 生成消息文本 (Caption)
# -------------------
def generate_caption():
    # 截取过长的 Commit Message (Telegram 限制 Caption 长度)
    short_msg = COMMIT_MESSAGE[:400] + "..." if len(COMMIT_MESSAGE) > 400 else COMMIT_MESSAGE

    # 构造 Markdown 消息
    caption = f"""**🎉 New Test Version Build Success!**
Version: `{TAG}`
Built by: [{COMMIT_AUTHOR_NAME}](https://github.com/{COMMIT_AUTHOR_GITHUB})

**✨ What's Changed:**
`{short_msg}`

**📂 Architecture Info:**
• `arm64-v8a`: 64位 (主流新手机)
• `armeabi-v7a`: 32位 (旧款手机)
• `x86/x86_64`: 模拟器/PC
• `universal`: 通用版

[Config File](https://t.me/Sesame_TK_Channel/306) | [Commit Details](https://github.com/{COMMIT_AUTHOR_GITHUB}/Sesame-TK/commit/{COMMIT_SHA})
"""
    return caption

# -------------------
# 主逻辑
# -------------------
async def main():
    if not valid_apks:
        print("❌ No valid APK files found via environment variables.")
        return

    print(f"🔄 Connecting to Telegram... (Target: {CHAT_ID})")
    client = TelegramClient('bot_session', API_ID, API_HASH)
    await client.start(bot_token=BOT_TOKEN)

    # 生成文案
    caption_text = generate_caption()
    
    # 遍历发送文件
    for index, path in enumerate(valid_apks):
        file_name = os.path.basename(path)
        print(f"📤 Uploading: {file_name} ...")
        
        try:
            # 核心逻辑：只有第一个文件带 Caption，实现“消息+文件”同框
            if index == 0:
                await client.send_file(
                    CHAT_ID, 
                    path, 
                    caption=caption_text, 
                    parse_mode='md'
                )
            else:
                # 后续文件只发文件
                await client.send_file(CHAT_ID, path)
            
            print(f"✅ Uploaded: {file_name}")
            
        except Exception as e:
            print(f"❌ Failed to upload {file_name}: {e}")

    await client.disconnect()
    print("🎉 All tasks finished.")

if __name__ == "__main__":
    asyncio.run(main())
