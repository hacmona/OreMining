# Minecraft用 鉱石採掘ミニゲーム

## ゲームの概要
このゲームはMinecraft内で制限時間内にさまざまな種類の鉱石を採掘し、その鉱石の種類に応じたポイントを獲得していくことが目的です。プレイヤーは採掘した鉱石ごとに異なるスコアを得られ、最終的な得点で競います。

## 開始方法
- **ゲーム開始**: 任意の場所で `/oreMining` コマンドを入力するとゲームが開始されます。
- **制限時間**: 5分間です。
- **装備の配布**: ゲーム開始時、メインハンドにネザライトのツルハシ、オフハンドに松明64個が配布されます。事前に持っていたアイテムは失われますのでご注意ください。
- **体力と空腹度**: ゲーム開始時には体力と空腹度が最大に回復され、ゲーム中は空腹度が減少しません。
- **採掘**: 地面や岩石を掘り、さまざまな鉱石を採掘します。同じ鉱石を連続で採掘するとボーナスポイントが加算されます。
- **安全性**: ゲーム中、モンスターからの攻撃ではダメージを受けませんが、落下やマグマには注意が必要です。
- **スコア表示**: 制限時間終了後、獲得したスコアが表示され、データベースに保存されます。
- **スコア確認**: `/oreMining list` コマンドで過去のスコアを確認できます。


https://github.com/hacmona/OreMining/assets/131163382/a9ba974c-9241-45f2-b8e2-338df69f3787


### 対応バージョン
- **Spigot**: 1.20.4
- **Minecraft**: 1.20.4

#### MySQLの設定
- **ユーザー名**: root
- **パスワード**: Pre07neo4!
- **データベース名**: spigot_server
- **テーブル名**: ore_player_score
- **URL**: jdbc:mysql://localhost:3306/spigot_server?serverTimezone=UTC

##### 独自要素
- **鉱石の種類と点数の表示**: ゲーム内で採掘される各鉱石には異なる点数が割り当てられており、採掘時に鉱石の種類と点数がメッセージに表示されます。
- **同種鉱石連続採掘ボーナス**: 同じ鉱石を連続で採掘することでボーナス点が付与されます。ゲームにより戦略性を持たせました。
- **ゲーム開始と制限時間のタイトルコール**: ゲームの開始とともに制限時間をプレイヤーに通知します。ゲーム開始の合図と制限時間の確認を分かりやすくしました。
- **特定時間経過アナウンス**: ゲーム中、残り時間が半分になると「残り時間はあと半分！」、残り1分になると「残り時間はあと1分！」とアナウンスされます。
- **敵ダメージ無効＆空腹度減少無し**: ゲーム中、プレイヤーは敵からのダメージを受けませんし、空腹度の減少もありません。これにより、ゲームに集中しやすくなっています。
- **敵出現**: 敵（エンティティ）を出現させているのは、ダメージ無効にすることである程度採掘に集中でき、かつ、敵が出現したほうがゲームが面白くなると思ったからです。
