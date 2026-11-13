# Hueno — Google Play listing ja-JP

Created 2026-11-13 from the en-US listing in [hueno-aso.md](../hueno-aso.md), section 3. Only features claimed in en-US are claimed here. Text inside code blocks is ready to paste into Play Console.

Character counts are Python `len()` (Play Console counts characters the same way for Japanese: one full-width character = 1).

## App title (22/30)

```
カメラで色判別・色の名前がわかる：Hueno
```

Keyword first, brand last, matching the en-US decision. Home-screen name stays "Hueno" (`app_name`).

## Short description (55/80)

```
カメラを向けるだけで色の名前がわかる色判別アプリ。カラーコード表示、写真からの色抽出、色弱・色覚のサポートに。
```

## Full description (1420/4000)

```
Huenoは、カメラを向けるだけで何色かがわかる色判別アプリです。どんなものにスマホを向けても、「濃い青」「明るい緑」「赤茶色」のように、ふだん使う言葉で色の名前を表示します。そのすぐ下に、より詳しい色名とカラーコード（16進数）も表示します。

色覚に特性のある方や色弱の方、塗料・布・メイクの色を合わせたい方、実物を見ながら描くアーティストのために作りました。

【これは何色？】
・カメラを向けるとすぐに色の名前が出るリアルタイム色検出
・まずはわかりやすい基本の色名（赤、オレンジ、黄色、緑、青、紫、ピンク、茶色、灰色、黒、白）を、明るい・濃いの違いとあわせて表示
・正確な色合いが知りたいときは、詳しい色名とカラーコードも
・タップで色の名前を読み上げ
・安定した読み取り：リング内の色を平均するので、色名がちらつきません

【読み取りの確かさがわかる】
・白い紙でワンタップ補正：白い紙に向けてタップするだけで、部屋の照明に合わせて補正します
・読み取りの確かさを「良好」「まずまず」「不安定」で表示し、「暗すぎます。明るくしてください」のようなヒントも出します
・正直な設計：スマホのカメラは測定器ではありません。読み取りが不確かなときは、Huenoがそう伝えます

【2つの色を比べる】
・色Aを保存してから色Bにカメラを向けると、「同じ」「近い」「違う」を判定
・違いも言葉で説明：「Bは緑で、少し明るいです」
・配線の見分け、状態ランプの確認、服の組み合わせ、壁と塗料の色見本の照合に便利です

【写真から色を選ぶカラーピッカー】
・写真やスクリーンショットを開き、タップして色を抽出
・拡大してリングを動かせば、1ピクセル単位で色を調べられます
・カラーコードをワンタップでコピー

【わかりやすい言葉で伝える色覚サポート】
多くの色アプリは「縹色」「鳶色」のような名前で答えます。Huenoはまず色の系統を、次に明るさや濃さを伝えます。赤と緑の見分けが難しい方も、それ以外のタイプの方も、すぐ行動につなげられる答えが得られます。どの配線か、どのシャツか、グラフのどの線か。TalkBackや大きな文字にも対応しています。

【保存して整理】
・保存した色の履歴。色名とカラーコードつき
・色に名前をつけて、コレクションにまとめられます
・ProならCSVに書き出し

【控えめな広告、わかりやすいアップグレード】
・カメラ画面と写真画面には広告を表示しません
・作業をさえぎる全画面広告はありません
・Hueno Proは買い切りで、定期購入ではありません。広告がなくなり、履歴と色の比較が無制限になり、部屋ごとに補正を保存できます
・購入前に試したい方は、短い動画を1本見るとProを6時間使えます

【こんな方に】
・身の回りのものの色を知りたい色弱・色覚に特性のある方
・DIYや住まいづくり：買いに行く前に塗料の色を調べる
・裁縫、編み物、ハンドメイド：糸、毛糸、布の色合わせ
・アーティスト：資料の影や肌の本当の色を調べる
・デザイナーや開発者：実物の色からカラーコードを取得

【ご注意】
色の読み取り結果は、カメラと照明によって変わります。より確かな結果を得るには、昼の光か明るい白色の照明の下で、先に補正を行ってください。Huenoは参考用のツールです。医療機器や認定された色彩測定器ではありません。
```

## Keywords and why

No Japanese search-volume or Play autocomplete data was collected for this listing. The choices below are judgment based on how Japanese users commonly describe these apps; check Play Console keyword reports 4–6 weeks after launch before changing the title.

| Keyword | Where | Why |
|---|---|---|
| 色判別 | Title, full description | Common way to say "identify color"; matches "color identifier", the main en-US keyword |
| 色の名前 / 色名 | Title, short, full | Core promise (en-US "color name"); people ask "この色の名前は？" |
| カメラ | Title, short, full | Pairs with 色判別 (色判別 カメラ), en-US "color identifier camera" |
| 何色 / 何色かわかる | Full | Natural form of "what color is this" (これは何色？) |
| 色弱 / 色覚 | Short, full | How Japanese users search for color-vision help. Written respectfully as 色覚に特性のある方 / 色弱の方; the older term 色盲 is avoided on purpose |
| カラーコード / 16進数 | Short, full | en-US "hex color code" |
| カラーピッカー / 写真 色 抽出 / スクリーンショット | Full | en-US "color picker from image" |
| 色見本 | Full, once | Users compare items with 色見本 (paint, thread, fabric). Used only for matching, not claiming a paint library (en-US says the same) |
| 色比較 / 2色を比べる | Full | en-US "compare two colors" |
| 色を調べる / 色検出 | Full | Synonym coverage for "color detector / finder / recognition", without stuffing |
| 読み上げ | Full | Read-aloud feature, relevant to accessibility searches |

## Play policy checks

- No "無料", "最高", "No.1", "ランキング" or similar in title or short description; none in the full description either.
- No emoji. Bullets use "・" and section headers use 【】.
- No competitor or brand names. 「縹色」「鳶色」 are traditional color names, used as the example of hard-to-understand names (en-US uses "arsenic"/"camel" the same way).
- No user testimonials, no medical claims; ends with the same disclaimer as en-US (not a medical device or certified instrument).
- Pro described as 買い切り (one-time purchase, no subscription), 6-hour pass via one video, as in en-US.
- Lines to delete if the feature is cut before release (same as en-US): TalkBack, CSV, 部屋ごとに補正を保存, 6時間.
