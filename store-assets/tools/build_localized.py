"""Builds the localized phone screenshots for the Play listing translations.

Captures come from a Pixel-sized emulator (1080x2400) with the app in each language: the camera
screens show the emulator's virtual scene, the photo, detail and history screens use
sample-swatches-source.jpg. The debug readout was hidden while capturing.

Usage: python3 build_localized.py <captures dir with one folder per locale> <output dir>
"""
import os, sys
import compose

SHOTS = ['1-camera', '2-color-names', '3-compare', '4-calibrate', '5-photo', '6-history']

# Same messages as the en-US captions in hueno-aso.md section 4. '|' marks a line break.
CAPTIONS = {
    'de': ['Draufhalten.|Farbe erkennen.', 'Farbnamen, die du|schon kennst', 'Gleich oder anders?|Zwei Farben vergleichen',
           'Mit einem weißen Blatt|kalibrieren', 'Farbe aus jedem|Foto bestimmen', 'Jede erfasste Farbe,|gespeichert'],
    'fr': ['Visez.|Lisez la couleur.', 'Des noms de couleurs|que vous connaissez', 'Pareilles ou pas ?|Comparez deux couleurs',
           'Calibrez avec une|feuille de papier blanc', 'Prélevez une couleur|dans une photo', 'Chaque couleur|capturée, enregistrée'],
    'es': ['Apunta.|Conoce el color.', 'Nombres de colores|que ya conoces', '¿Iguales o distintos?|Compara dos colores',
           'Calibra con una|hoja de papel blanco', 'Toma un color|de cualquier foto', 'Cada color que capturas,|guardado'],
    'ja': ['かざすだけ。|色がわかる。', 'なじみのある|色の名前で', '同じ？違う？|2つの色を比べる',
           '白い紙1枚で|かんたん補正', '写真から|色を読み取る', '読み取った色を|すべて保存'],
    'ko': ['비추면|색이 보여요', '익숙한|색 이름으로', '같을까, 다를까?|두 색 비교',
           '흰 종이 한 장으로|간편 보정', '어떤 사진에서도|색 추출', '찾은 색은|모두 저장'],
}

# Emulator capture: status bar above, gesture bar below.
TOP, BOTTOM = 136, 2330

if __name__ == '__main__':
    src, dst = sys.argv[1], sys.argv[2]
    for lang, captions in CAPTIONS.items():
        os.makedirs(os.path.join(dst, lang), exist_ok=True)
        for shot, caption in zip(SHOTS, captions):
            compose.screenshot(os.path.join(src, lang, shot + '.png'), caption,
                               os.path.join(dst, lang, f'{lang}-{shot}.png'), TOP, BOTTOM, lang)
            print(lang, shot)
