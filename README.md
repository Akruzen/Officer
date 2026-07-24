<div align="center">

<img src="https://github.com/Akruzen/Officer/blob/master/app/src/main/ic_launcher-playstore.png" width="128" height="128" />

# Officer
**Keep your Phone, <i>your</i> Phone.**

<p align="center">
  <strong>Officer</strong> is a security application which prevents shutting down your phone from the lock screen. This prevents thieves from powering off your phone so that you can track it easily.
</p>

<p align="center">
  <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/Android/android2.svg">
  <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/Java/java2.svg">
  <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/AndroidStudio/androidstudio3.svg">
  <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/LicenceGPLv2/licencegplv21.svg">
  <img src="https://m3-markdown-badges.vercel.app/stars/3/3/akruzen/officer">
</p>

<table align="center">
  <tr>
    <td><img src="https://github.com/user-attachments/assets/076ea377-54b1-4190-8dc9-5223e66e33cf" alt="Screenshot 1" width="200" style="border-radius:26px;"/></td>
    <td><img src="https://github.com/user-attachments/assets/47e10213-b4ab-4e04-821d-afba8dfe3300" alt="Screenshot 3" width="200" style="border-radius:26px;"/></td>
    <td><img src="https://github.com/user-attachments/assets/bf71b120-3418-445e-a122-feb3bd6bdd28" alt="Screenshot 2" width="200" style="border-radius:26px;"/></td>
  </tr>
</table>

</div>

---

## Appearing on:

### [Sam Beckman](https://www.youtube.com/@sambeckman)
<p align="left">
  Watch it here!
  <a href="https://youtu.be/0Q2ln01LmS0?si=rZfTzyS_qaygzhDu&t=133">
    <img src="https://img.shields.io/badge/YouTube-FF0000?style=for-the-badge&logo=youtube&logoColor=white">
  </a>
</p>
<img width="500" height="340" alt="image" src="https://github.com/user-attachments/assets/59a4b664-d159-43cc-8804-ef04cfbef60b" />

---

## Features:

|  |  |
| --- | --- |
| <img src="https://github.com/Akruzen/Officer/blob/master/app/src/main/ic_launcher-playstore.png" width="128" height="128" /> | **Switch off prevention**: When your screen is locked, any power off attempts from the power menu immediately puts your display to sleep. |
| <img width="128" height="128" alt="Strict Security" src="https://github.com/user-attachments/assets/4b30f040-550b-43b8-9af0-5572d13b2cd8" /> | **Strict Security**: For extra security, you can optionally keep putting your display to sleep continuously for a specific time after a power off attempt has been made from the lock screen. |
| <img width="128" height="128" alt="Group 127" src="https://github.com/user-attachments/assets/0ea1c149-f953-4c56-a7ea-15d19e700e99" /> | **SMS Alert**: Send an SMS with your phone's location when a power off attempt has been made from the lock screen. |
| <img width="128" height="128" alt="Group 129" src="https://github.com/user-attachments/assets/47562894-f4c2-4505-804b-b9b1c3917611" /> | **Custom Trigger**: Different phones use different triggers for the power menu, so there\'s no one-size-fits-all solution. With custom trigger you can choose your phone specific trigger. |
| <img width="128" height="128" alt="Group 130" src="https://github.com/user-attachments/assets/c242aaa4-f6e7-4cf6-9ed0-284517dc57d8" /> | **Broadcast Event**: Broadcast an event when someone tries to power off from the lock screen. You can use automation routines with third party apps like Tasker and Macrodroid to trigger further actions on your phone.  |

## Inspiration

<img width="500" height="273" alt="art_theft" src="https://github.com/user-attachments/assets/790f0baa-8950-47a1-979c-f56fd1ea1f7d" />
<p align="left">
  Officer was born out of my personal experience. I was returning home one evening in a rickshaw. When I got off to pay the driver, I realized I had left my phone on the seat. The driver immediately sped off, stealing       my phone. I tried to track it using Google's 'Find My Device', but unfortunately, he had switched it off. I hope my app helps people buy time to track their devices if this happens to them.
</p>

---

## Working
Using Android's Accessibility services, Officer monitors system UI events. Whenever it detects that a power menu is getting triggered while your screen is locked, it immediately puts the display to sleep. This way, the it becomes difficult to power off your phone.

---

## Installation
Installing this app will require you to follow a process. Since this app uses Accessibility service, it won't be as simple as downloading the APK and installing it.
1. Download the latest APK file from the [Releases](https://github.com/Akruzen/Officer/releases) section.
2. Open Google Play Store on your phone, and tap on your profile picture on the top right.
3. Tap on 'Play Protect'
4. Tap on 'Settings' icon on the top right.
5. Turn off the 'Scan apps with Play Protect' switch, and tap 'Pause'. I know this sounds shady, but it isn't. I have explained in the section below why is this required.
6. Install the APK normally.
7. Open the app and proceed to grant permissions.
8. Granting Accessibility Permission:
   i. Go to 'Accessibility Settings' of your phone and try to allow 'Officer'. It will probably show a message titled 'Restricted Setting'.
   ii. Go to Officer's App info setting. Click on the three dots menu on the top right and tap 'Allow Restricted Settings'
   iii. Again, go to 'Accessibility Settings'. You will now be able to grant this permission.

### Why does installation require this workaround?
Granting an 'Accessibility' permission to an app can potentially have unwanted effects, as this is a very sensitive permission. To prevent misuse of this permission, Google's latest update introduces friction in granting this permission so that people don't fall for scammers. This is one of the reasons I kept **Officer** as an open-source app. When you know what is happening behind the scenes in an app, you got nothing to worry about!

---

## Contact and feedback

You can drop me a message at my discord ID `Akruzen#2652`. If you find any issues with the app, feel free to [open a new issue](https://www.google.com/search?q=https://github.com/Akruzen/Officer/issues). Thanks!
