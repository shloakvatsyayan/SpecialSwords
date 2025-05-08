Thought for 4 seconds

# SpecialSwords
## [Resource pack Recommended](https://github.com/bonknetwork/SpecialSwords-Resourcepack/releases/)

**Version:** 1.2
**Minecraft:** Paper 1.21.4+  
**Authors:** Pyro from [BonkMC](https://bonkmc.net) - [Discord](https://discord.gg/bonknetwork)

SpecialSwords is a lightweight Paper plugin that adds an over-powered, visually striking “SMASHER” sword to your server. It’s perfect for PvE arenas, adventure maps, or just chaotic fun with friends. With a few quick clicks, players can launch themselves sky-high, slam the ground with seismic force, and watch spectacular particle and sound effects unfold.

---

## 🚀 Features

- **SMASHER Sword**  
  A custom Netherite sword called the “SMASHER,” complete with a bold, underlined red name and a full suite of hidden enchantments. It stands out in any hotbar.

- **Three-Click “SMASH” Activation**  
  Right-click three times in rapid succession to transform your SMASHER into a mighty mace and blast yourself upward.

- **Ground-Slam Finish**  
  If you don’t use any other ability mid-air, landing will automatically slam you back into the ground, restoring your SMASHER and delivering an earth-shaking impact.

- **Cooldown & Action-Bar Prompts**  
  After unleashing a SMASH, the ability goes on cooldown. Animated action-bar messages keep players informed of recharge progress.

- **Seamless Command Integration**  
  Instantly grant a SMASHER to any player with a single command:  
  ```txt
  /specialswords give smasher

No special permissions are required by default.

* **Automatic Cleanup**
  Any “lifted mace” items that fall to the ground are removed automatically, keeping your world tidy.

---

## 📥 Installation

1. **Download the JAR**
   Place the `SpecialSwords.jar` file into your server’s `plugins/` directory.

2. **Restart or Reload**
   Restart your server (recommended) or run `/reload` if necessary, however using `/reload` is HIGHLY discouraged.

3. **Install the Resource Pack (optional but suggested)**
   Download the [SpecialSwords Resource Pack](https://github.com/bonknetwork/SpecialSwords-Resourcepack/releases/) and add it to your server.

4. **Verify**
   Look for “SpecialSwords enabled!” in the console log to confirm successful loading.

That’s all—no extra configuration files needed.

---

## 🛠️ Usage

1. **Give Yourself a SMASHER**

   ```txt
   /specialswords give smasher
   ```

   You’ll receive a custom Netherite sword named **SMASHER** in your inventory.

2. **Unleash the SMASH**

    * **Right-click** three times quickly: you vault into the air wielding a molten mace.
    * **Land** to automatically slam down, returning the SMASHER to your hand.

3. **Watch the Cooldown**
   After your SMASH attack, your action bar displays “Recharging…” with animated dots until the ability is ready again.

---

## ⚙️ Configuration

SpecialSwords has no external configuration. All cooldowns, effects, and strengths are baked in. To customize values, recompile from source with your desired adjustments.

---

## ❓ FAQ

**Q: Can I use this on non-Paper servers?**
A: No. SpecialSwords relies on Paper 1.21.4 APIs for custom item models and Brigadier command registration.

**Q: Does it require permissions?**
A: By default, no. Anyone can run `/specialswords give smasher`. Use a permissions plugin if you want to restrict access.

**Q: Will this impact performance?**
A: No. The plugin runs two lightweight repeating tasks (cleanup and action-bar updates) and automatically disposes of its own dropped items.

---

## 🤝 Support & Contributions

Found a bug or have a feature request? Open an issue or submit a pull request on the [GitHub repository](https://github.com/yourusername/SpecialSwords).

---

## 📜 License

[Special Swords](https://github.com/bonknetwork/SpecialSwords) © 2025 by [Shloak Vatsyayan](https://github.com/shloakvatsyayan) is licensed under [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/?ref=chooser-v1)  
[![CC](https://mirrors.creativecommons.org/presskit/icons/cc.svg?ref=chooser-v1)](https://creativecommons.org/licenses/by-nc-sa/4.0/?ref=chooser-v1)
[![BY](https://mirrors.creativecommons.org/presskit/icons/by.svg?ref=chooser-v1)](https://creativecommons.org/licenses/by-nc-sa/4.0/?ref=chooser-v1)
[![NC](https://mirrors.creativecommons.org/presskit/icons/nc.svg?ref=chooser-v1)](https://creativecommons.org/licenses/by-nc-sa/4.0/?ref=chooser-v1)
[![SA](https://mirrors.creativecommons.org/presskit/icons/sa.svg?ref=chooser-v1)](https://creativecommons.org/licenses/by-nc-sa/4.0/?ref=chooser-v1)