# SudoVoid — Android (C-SCAN edition)

A single-purpose Android app with the same SudoVoid look (dark navy background, animated-style
perspective dot grid, glowing cyan/yellow monospace text) that runs **one built-in program**:
the C-SCAN disk scheduling algorithm from `DCscan.cpp`, re-implemented natively in Kotlin.

No compiler, no Termux, no internet — the algorithm's logic is baked directly into the app,
so it launches instantly and works fully offline. This is the "simple version" — the full
compiler-in-a-box (Alpine + proot, run arbitrary C++ files) is a separate, bigger project
saved for later.

## How to build

1. Install **Android Studio** (any recent version — Koala/Ladybug or newer):
   https://developer.android.com/studio
2. Open Android Studio → **Open** → select this `sudovoid-android` folder.
3. Let it **Sync Gradle** (first sync downloads dependencies — needs internet, takes a
   couple of minutes). If prompted to upgrade AGP/Gradle, you can accept — should still work.
4. Plug in your Android phone via USB with **Developer Options → USB debugging** enabled
   (or use an emulator), then click the green **Run ▶** button — or
   **Build → Build Bundle(s)/APK(s) → Build APK(s)** to get an installable `.apk` directly
   under `app/build/outputs/apk/debug/`.

## What it does

1. On launch, prompts: `Enter number of requests:`
2. Type a number, hit Enter/Done — then enter that many request values one at a time
   (matches the `cin >> req[i]` loop in the original).
3. Then `Enter initial head position:` and `Enter disk extremity:`.
4. Shows the seek sequence and total head movement, exactly like the C++ version:
   ```
   Seek Sequence:
   50 -> 60 -> 79 -> 92 -> 176 -> 199 -> 0 -> 34

   Total Head Movement = 382 cylinders
   ```
5. Tap **[ RESTART ]** top-right to run it again without closing the app.

## Files
- `MainActivity.kt` — all the terminal UI logic + the C-SCAN algorithm itself
  (`runCScan()` — line-for-line the same logic as `DCscan.cpp`'s `sort()` + `main()`).
- `GridBackgroundView.kt` — custom `Canvas` view drawing the perspective dot-grid background.
- `activity_main.xml` — layout: title bar, scrolling output area, stdin input bar.

## Testing online — no Android Studio needed

If you don't want to install Android Studio locally yet, you can build and test entirely in
the cloud:

1. **Push this folder to a GitHub repo** (create a new repo on github.com, then from inside
   this folder):
   ```bash
   git init
   git add .
   git commit -m "SudoVoid C-SCAN android app"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/sudovoid-android.git
   git push -u origin main
   ```
2. This repo already includes `.github/workflows/build-apk.yml` — as soon as you push,
   **GitHub Actions** automatically builds the APK in the cloud (free for public repos).
   Go to your repo's **Actions** tab and watch it run (~3-5 min).
3. Once it finishes (green checkmark), open that run → scroll to **Artifacts** →
   download `sudovoid-debug-apk` (a zip containing `app-debug.apk`).
4. Go to **https://appetize.io** → sign up free → **Upload** → select `app-debug.apk`.
   It launches a real Android emulator in your browser within seconds — tap around, test
   the C-SCAN flow, no phone or Android Studio required.

Free tier on Appetize.io gives ~100 minutes/month of testing, which is plenty for this.

## Next step (saved for later)
Turning this into the full **SudoVoid-Android**: a general-purpose terminal that can open and
run *any* `.cpp`/`.py` file you pick from storage, using a bundled minimal Linux environment
(Alpine + proot) so it compiles on-device without Termux. That's a bigger build — this app is
the fast, working slice for the C-SCAN use case specifically.
