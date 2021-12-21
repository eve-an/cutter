GAME_START = 6270
FPS = 30
TIMES = ((0, 49), (6, 4), (7,59), (11,54), (16,41))


for time in TIMES:
    minutes, seconds = time

    all_seconds = 60 * minutes + seconds
    frame = all_seconds * FPS + GAME_START

    print(f"{minutes}:{seconds} = {frame} Frame")