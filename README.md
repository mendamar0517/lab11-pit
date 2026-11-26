# Лаборатори №11: Mutation testing — PIT-ээр тестийн чанараа хэмжих

F.CSA313 — Программ хангамжийн чанарын баталгаа ба тест (2026)

- **Овог нэр:** О.Мэнд-Амар
- **Оюутны код:** B232270001

## Ажлын орчин

`java -version` ба `mvn -version`-ий гаралт (бүтэн хувилбар: [`results/env.txt`](results/env.txt)):

```
openjdk version "17.0.19" 2026-04-21
OpenJDK Runtime Environment Homebrew (build 17.0.19+0)
OpenJDK 64-Bit Server VM Homebrew (build 17.0.19+0, mixed mode, sharing)
Apache Maven 3.9.16 (2bdd9fddda4b155ebf8000e807eb73fd829a51d5)
Maven home: /opt/homebrew/Cellar/maven/3.9.16/libexec
Java version: 26.0.1, vendor: Homebrew, runtime: /opt/homebrew/Cellar/openjdk/26.0.1/libexec/openjdk.jdk/Contents/Home
Default locale: en_MN, platform encoding: UTF-8
OS name: "mac os x", version: "26.6.2", arch: "aarch64", family: "mac"
```

Тэмдэглэл: shell-ийн `java` нь 17, Maven нь Homebrew-ийн JDK 26 runtime дээр ажилладаг. `pom.xml`-д `maven.compiler.release=17` тул байт код Java 17-ийнх байна.

Хэрэгслүүд: JUnit 5.10.2, JaCoCo 0.8.12, PIT 1.16.1 (+ pitest-junit5-plugin 1.2.1), мутацийн операторууд `STRONGER`.

## Хэрхэн дахин ажиллуулах вэ

```bash
mvn clean verify                                            # тест + JaCoCo тайлан (target/site/jacoco)
mvn test-compile org.pitest:pitest-maven:mutationCoverage   # PIT тайлан (target/pit-reports)
```

---

## 1. Эхний хэмжилт (Starter, тестийг өөрчлөхөөс өмнө)

Нотолгоо: [`results/mvn-verify-start.txt`](results/mvn-verify-start.txt), [`results/jacoco-start.csv`](results/jacoco-start.csv), [`results/pit-start.txt`](results/pit-start.txt)

### JaCoCo

![JaCoCo — Starter](screenshots/jacoco-start.png)

`results/jacoco-start.csv`: `LINE_MISSED=0, LINE_COVERED=25` → **Line 100% (25/25)**; `BRANCH_MISSED=3, BRANCH_COVERED=38` → **Branch 38/41 (92%)**. Дутуу 3 салаа бүгд `scholarship()`-д байгаа (83%).

### PIT

![PIT — Starter](screenshots/pit-start.png)

`results/pit-start.txt`-ийн төгсгөл:

```
>> Line Coverage (for mutated classes only): 25/25 (100%)
>> Generated 63 mutations Killed 17 (27%)
>> Mutations with no coverage 0. Test strength 27%
```

**Mutation Coverage = 17/63 (27%)**, амьд үлдсэн мутант 46.

### Ажиглалт: Line Coverage ба Mutation Coverage-ийн зөрүү

Line Coverage 100% байгаа нь кодын мөр бүр тестийн явцад *ажилласан* гэсэн утгатай. Гэхдээ тест тэр мөрийн *үр дүнг шалгасан* эсэхийг хэлдэггүй. Мутант бүр coverage-тэй (`no coverage 0`) атал 63-аас ердөө 17 нь алагдсан. Энэ нь тестүүд кодыг гүйлгэж байгаа ч `assertNotNull`, `assertTrue(0 ≤ x ≤ 100)` гэх мэт сул assertion-ууд буруу утгыг ч "зөв" гэж хүлээн авдгийг харуулж байна. Өөрөөр хэлбэл 73 пунктийн зөрүү бол тестийн багцын *алдаа илрүүлэх чадвар* дутуу байгаагийн хэмжүүр юм.

---

## 2. Хэсэг A: Амьд мутантуудын ангилал

Эх сурвалж: `target/pit-reports/mn.edu.must.sqat/ScholarshipCalculator.java.html` (Starter ажиллагаа). 46 SURVIVED мутантыг мөр ба шалтгаанаар нь бүлэглэв. "Тоо" багана нь тухайн мөрөнд тэр шалтгаанаар амьд үлдсэн мутантын тоо. Нийлбэр нь 46.

Мутацийн тайлбар (PIT 1.16.1, `STRONGER`):
- *changed conditional boundary*: `>=` ↔ `>`, `<` ↔ `<=`
- *removed conditional – replaced comparison check with true*: харьцуулалтын үсрэлтийг хассан, тухайн `if`-ийн нөхцөл үргэлж нэг талдаа ажиллана
- *removed conditional – … with false*: эсрэг тал нь үргэлж ажиллана
- *replaced return value*: буцаах утгыг `""` / `0` / `0.0`-ээр сольсон
- *Replaced double addition with subtraction*: `+=` → `-=`
- *Changed switch default to be first case*: `switch`-ийн default ба эхний case-ийн үсрэх хаягийг сольсон

| Мөр | Мутаци (PIT-ийн тайлбар) | Тоо | Ангилал | Үндэслэл (1 өгүүлбэр) | Арга хэмжээ |
|---|---|---|---|---|---|
| 20 | changed conditional boundary: `score < 0` → `<= 0`, `score > 100` → `>= 100` | 2 | Тест дутуу | 0 ба 100 нь хүчинтэй оролт боловч мутант дээр exception шиддэг, ийм зааг утгын тест байхгүй тул бодит зан төлөв өөрчлөгдөж байгааг хэн ч барихгүй. | B2: `letterGrade(0)="F"`, `letterGrade(100)="A"` |
| 23–27 | replaced return value with `""` (`"A"`,`"B"`,`"C"`,`"D"`,`"F"` → `""`) | 5 | Тест дутуу | `assertNotNull("")` давна. Хоосон мөр бол буруу үсгэн дүн тул зан төлөв өөрчлөгдсөн ч assertion хэт сул. | B1: `assertEquals("A", letterGrade(95))` гэх мэт яг утга |
| 23–26 | removed conditional → true / → false (`score >= 90/80/70/60` бүрт 2) | 8 | Тест дутуу | "true" бол доод мужийн оноо ч дээд үсгийг ("A") буцаана, "false" бол 95 оноо "B" болно. Энэ нь бодит зан төлөвийн өөрчлөлт боловч `assertNotNull` ялгахгүй. | B1: муж бүрийн оноонд яг үсгэн дүн |
| 23–26 | changed conditional boundary: `>= 90/80/70/60` → `> 90/80/70/60` | 4 | Тест дутуу | Яг 90, 80, 70, 60 оноо мутант дээр нэг доод үсэг авдаг (90 → "B"). Тест эдгээр зааг утгыг огт дуудаагүй. | B2: 90/89.99, 80/79.99, 70/69.99, 60/59.99 |
| 36 | Changed switch default to be first case | 1 | Тест дутуу | Мутант "A" дүнг default руу илгээж 4 биш 0 нэмдэг тул голч буурна, гэвч `0 ≤ g ≤ 4` нөхцөл үүнийг ялгахгүй. | B1: `gpa({95,85}) == 3.5`; B2: бүх үсгийг агуулсан массив |
| 38 | Replaced double addition with subtraction (`total += 3` → `-= 3`) | 1 | Тест дутуу | `{95,85}`-ийн голч 3.5 биш 0.5 болдог ч энэ нь [0,4] мужид багтаж assertion давна. | B1: `assertEquals(3.5, …)` |
| 40 | Replaced double addition with subtraction (`total += 1` → `-= 1`) | 1 | Тест дутуу | `{75,65}`-ийн голч 1.5 биш 0.5 болох ч мужийн шалгалт давна. | B1: `assertEquals(1.5, …)` |
| 44 | replaced double return with 0.0d | 1 | Тест дутуу | 0.0 нь [0,4] мужид багтдаг тул ямар ч голчийг 0 болгосон алдааг тест барихгүй. | B1: яг голчийг шалгах |
| 48 | changed conditional boundary: `gpa < 0` → `<= 0`, `credits < 0` → `<= 0` | 2 | Тест дутуу | gpa=0 ба credits=0 нь хүчинтэй оролт боловч мутант exception шиднэ, ийм зааг тест алга. | B2: `scholarship(0.0, 0, false) == 0`, `scholarship(3.0, 0, false) == 50` |
| 48 | removed conditional → true (`gpa < 0` шалгалтын үсрэлт хасагдсан) | 1 | Тест дутуу | Мутант сөрөг голчийг хүлээн авдаг (exception шидэхгүй), гэвч тест зөвхөн 4.1 ба credits=-1-ийг шалгадаг. | B2: `scholarship(-0.01, 10, false)` → IAE |
| 51 | removed conditional → true / → false (`hasViolation`) | 2 | Тест дутуу | "false" бол зөрчилтэй оюутан 100% авна, "true" бол бүх оюутан 0% авна. Хоёулаа [0,100] мужид багтана. | B1: `scholarship(4.0,20,true) == 0`, `scholarship(3.9,16,false) == 100` |
| 52 | replaced int return with 0 (`return 100`) | 1 | Тест дутуу | 100%-ийн тэтгэлгийг 0 болгосон ч `0 ≤ s ≤ 100` давна. | B1: `assertEquals(100, …)` |
| 52 | removed conditional → false: `gpa >= 3.8`, `credits >= 15` | 2 | Тест дутуу | Нөхцөл үргэлж худал бол (3.9, 16) нь 100 биш 70 авна, гэвч мужийн шалгалт ялгахгүй. | B1: яг 100 |
| 52 | removed conditional → true: `gpa >= 3.8`, `credits >= 15` | 2 | Тест дутуу | Нэг нөхцөл нь үргэлж үнэн бол (3.79, 15) эсвэл (3.8, 14) 100 авна. Энэ хослолыг тест дуудаагүй. | B2: `(3.79,15)=70`, `(3.8,14)=70` |
| 52 | changed conditional boundary: `gpa >= 3.8` → `> 3.8`, `credits >= 15` → `> 15` | 2 | Тест дутуу | Яг (3.8, 15) мутант дээр 70 болдог, зааг тест байхгүй. | B2: `(3.8,15)=100` |
| 53 | replaced int return with 0 (`return 70`) | 1 | Тест дутуу | 70%-ийг 0 болгосон ч мужид багтана. | B1: `assertEquals(70, …)` |
| 53 | removed conditional → false: `gpa >= 3.4`, `credits >= 12` | 2 | Тест дутуу | (3.5, 13) нь 70 биш 50 болно, assertion сул. | B1: яг 70 |
| 53 | removed conditional → true: `gpa >= 3.4`, `credits >= 12` | 2 | Тест дутуу | (3.39, 12) эсвэл (3.4, 11) 50 биш 70 авна, тест ийм оролт дуудаагүй. | B2: `(3.39,12)=50`, `(3.4,11)=50` |
| 53 | changed conditional boundary: `>= 3.4` → `> 3.4`, `>= 12` → `> 12` | 2 | Тест дутуу | Яг (3.4, 12) мутант дээр 70 биш 50 болно. | B2: `(3.4,12)=70` |
| 54 | replaced int return with 0 (`return 50`) | 1 | Тест дутуу | 50%-ийг 0 болгосон ч мужид багтана. | B1: `assertEquals(50, …)` |
| 54 | removed conditional → true / → false (`gpa >= 3.0`) | 2 | Тест дутуу | "true" бол 2.0 голчтой оюутан 50% авна, "false" бол 3.1 голчтой оюутан 0% авна. Хоёулаа мужид багтана. | B1: `(3.1,5)=50`, `(2.0,1)=0` |
| 54 | changed conditional boundary: `gpa >= 3.0` → `> 3.0` | 1 | Тест дутуу | Яг 3.0 голч мутант дээр 0 болно, зааг тест байхгүй. | B2: `(3.0, 0)=50`, `(2.99, 20)=0` |
| | **Нийт** | **46** | **Тест дутуу 46, Equivalent 0** | | |

**Equivalent мутант байхгүй гэсэн дүгнэлтийн үндэслэл.** Дээрх 46 мутант бүрийн хувьд гаралт нь эх кодоос ялгарах тодорхой оролтыг хүснэгтийн «Арга хэмжээ» баганад нэрлэсэн. Тухайлбал `double`-ийн зааг утга (90.0, 3.8, 3.4, 3.0) нь кодын литералтай яг адил давхар нарийвчлалтай утга тул `>=` ба `>` хоёр тэр цэг дээр заавал өөр үр дүн өгнө. Ийм ялгах оролт олдож байгаа бол тухайн мутант equivalent биш, харин тест дутуу гэсэн үг. Иймд B хэсгийн дараа бүх мутант алагдах ёстой гэсэн таамаглал дэвшүүлж, үүнийг PIT-ээр шалгана.

Хоёр бүлэг ажиглагдав:
- **Assertion сул (B1-ээр засна): 28 мутант.** Эдгээр нь *replaced return value*, *addition → subtraction*, *switch* болон одоо байгаа оролтоор ялгагдах *removed conditional* мутантууд. Тест кодыг аль хэдийн зөв оролтоор дуудаж байгаа ч үр дүнг нь шалгадаггүй.
- **Зааг/оролт дутуу (B2-оор засна): 18 мутант.** Эдгээр нь *changed conditional boundary* бүгд, мөн одоогийн оролтоор ялгагдахгүй *removed conditional → true* мутантууд (мөр 48, 52, 53). Хүчтэй assertion ч гэсэн тохирох оролт дуудагдахгүй бол тэдгээрийг алж чадахгүй.

---

## 3. Хэсэг B: Тестүүдийг хүчтэй болгох

### Гурван хэмжилтийн явц

| Хэмжилт | Commit | Тестийн тоо (JUnit) | Mutation Coverage | Test Strength | Амьд | Нотолгоо |
|---|---|---|---|---|---|---|
| Эхлэл | `Эхний хэмжилт` | 6 | **17/63 (27%)** | 27% | 46 | [`results/pit-start.txt`](results/pit-start.txt) |
| B1-ийн дараа | `B1: …` | 6 | **45/63 (71%)** | 71% | 18 | [`results/pit-B1.txt`](results/pit-B1.txt) |
| B2-ийн дараа (эцсийн) | `B2: …` | 34 | **63/63 (100%)** | 100% | 0 | [`results/pit-B2.txt`](results/pit-B2.txt) |

Бүх гурван хэмжилтэд Line Coverage 25/25 (100%) хэвээр байсан. Хамралт огт өөрчлөгдөөгүй атал mutation score 27% → 71% → 100% болж өссөн.

`results/pit-*.txt` файлуудын мөрүүд:

```
pit-start.txt: >> Generated 63 mutations Killed 17 (27%)
pit-B1.txt:    >> Generated 63 mutations Killed 45 (71%)
pit-B2.txt:    >> Generated 63 mutations Killed 63 (100%)
```

### B1: Assertion-уудыг хүчтэй болгох (+28 мутант)

Starter-ийн *оролтуудыг өөрчлөлгүйгээр* зөвхөн шалгалтыг солив:

| Өмнө (сул) | Дараа (яг утга) |
|---|---|
| `assertNotNull(calc.letterGrade(95))` | `assertEquals("A", calc.letterGrade(95))` (B, C, D, F-д мөн адил) |
| `assertTrue(g1 >= 0 && g1 <= 4)` | `assertEquals(3.5, calc.gpa({95,85}), 1e-9)`, `1.5`, `0.0` |
| `assertTrue(s1 >= 0 && s1 <= 100)` | `assertEquals(100, …)`, `70`, `50`, `0`, зөрчилтэй бол `0` |

Үр дүн: Хэсэг A-д "B1" гэж тэмдэглэсэн 28 мутант бүгд алагдсан (17 → 45), таамаглалтай яг таарсан. Үлдсэн 18 нь бүгд B2 гэж ангилсан мутантууд байв: `changed conditional boundary` 13 (мөр 20, 23–26, 48, 52, 53, 54), мөр 48, 52, 53-ын `removed conditional → true` 5. Энэ нь хүчтэй assertion ч гэсэн тохирох оролт дуудагдахгүй бол boundary мутантыг алж чадахгүйг харуулж байна.

### B2: Зааг утгын тестүүд нэмэх (+18 мутант)

- `letterGrade`: `@ParameterizedTest`-ээр 100, 90/89.99, 80/79.99, 70/69.99, 60/59.99, 0, мөн хязгаараас яг гадна орших −0.01 ба 100.01
- `gpa`: switch-ийн үсэг бүрийг дангаар (A=4 … F=0), бүх үсгийг нэг массивт (2.0), зааг оноонуудын голч (2.5 ба 1.5), массив дотор буруу оноо
- `scholarship`: 3.8/15 = 100, 3.79/15 = 70, 3.8/14 = 70, 3.4/12 = 70, 3.39/12 = 50, 3.4/11 = 50, 3.0/0 = 50, 2.99/20 = 0, хүчинтэй хязгаар (0.0, 0) = 0, (4.0, 15) = 100, (4.0, 0) = 50, зөрчилтэй (3.8, 15) = 0, хязгаараас гадна −0.01, 4.01, credits −1

Үр дүн: **63/63 (100%)**. Хэсэг A-д бүх 46 мутантыг "Тест дутуу" гэж ангилсан, equivalent гэж үзсэн мутант байгаагүй. Тиймээс Test Strength мөн 100% бөгөөд equivalent мутантыг хасах шаардлага гараагүй.

### Эцсийн PIT тайлан

![PIT — эцсийн](screenshots/pit-final.png)

![PIT — эх код (бүх мөр ногоон)](screenshots/pit-final-source.png)

---

## 4. Хэсэг C: Гараар нэг мутант (PIT-гүйгээр)

### Туршилт

`scholarship()`-ийн 52-р мөрийг гараар өөрчлөв:

```diff
-        if (gpa >= 3.8 && credits >= 15) return 100;
+        if (gpa >  3.8 && credits >= 15) return 100;
```

`mvn test 2>&1 | tee results/mvn-test-manual-mutant.txt` ([нотолгоо](results/mvn-test-manual-mutant.txt)):

```
[ERROR] Tests run: 34, Failures: 1, Errors: 0, Skipped: 0
[ERROR] mn.edu.must.sqat.ScholarshipCalculatorTest.scholarship_boundaries(double, int, int)[1] -- Time elapsed: 0.005 s <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <100> but was: <70>
[ERROR]   ScholarshipCalculatorTest.scholarship_boundaries:157 expected: <100> but was: <70>
[INFO] BUILD FAILURE
```

- **Унасан тест:** `scholarship_boundaries[1]`, өөрөөр хэлбэл `@CsvSource`-ийн эхний мөр `"3.8, 15, 100"` (B2-д нэмсэн зааг тест)
- **Алдааны мөр:** `ScholarshipCalculatorTest.java:157`, `expected: <100> but was: <70>`. Мутант дээр голч яг 3.8 үед `3.8 > 3.8` худал болж, 70%-ийн дүрэм рүү унасан.
- 34 тестээс зөвхөн энэ нэг нь унасан. Starter-ийн багц энэ мутантыг барихгүй байсан: Starter дээр PIT-ийн мөр 52-ын `changed conditional boundary` SURVIVED байсан. Тиймээс энэ ялгааг B2-ын зааг тест гаргаж ирсэн.
- Дараа нь `git restore src/main`-ээр буцаасан. Мутацитай кодыг commit хийгээгүй. `git diff 43f38f7 -- src/main` хоосон буюу src/main нь Starter-тэй яг ижил.

### Өөрийн зохиосон EQUIVALENT мутант

`gpa()`-ийн switch-ийн `case "D"`-ийн `break`-ийг устгах:

```diff
                 case "C": total += 2; break;
-                case "D": total += 1; break;
+                case "D": total += 1;
                 default:  break;
```

`break` устсан ч "D"-ийн дараа ажиллах `default` салаа нь зөвхөн `break` агуулдаг хоосон код тул fall-through болоход `total`-д юу ч нэмэгдэхгүй, ямар ч оролтод гаралт яг ижил байна. Ажиглагдах зан төлөв ямар ч оролт дээр өөрчлөгддөггүй тул үүнийг алах тест бичих боломжгүй, ийм мутант зөвхөн mutation score-ийг зохиомлоор бууруулдаг тул Test Strength тооцохдоо хасах ёстой.

---

## 5. Дүгнэлт

Line Coverage нь кодын аль мөр *ажилласныг* хэмждэг бол mutation score нь тест *алдааг барьж чадах эсэхийг* хэмждэг. Энэ лабад Line 100% (25/25) хэвээр байхад mutation score 27%-иас 100% хүртэл өссөн нь хамралт нь тестийн чанарын зайлшгүй нөхцөл боловч хангалттай нөхцөл биш гэдгийг тоогоор харууллаа. Starter-ийн `assertNotNull` болон мужийн `assertTrue` шалгалтууд кодыг гүйлгэдэг ч буцаах утга `""` эсвэл `0` болсныг ч "зөв" гэж хүлээн авдаг байсан. Assertion-ыг яг утгатай болгоход (B1) 28 мутант алагдсан, гэвч `>=` ↔ `>` төрлийн boundary мутантуудыг алахын тулд яг зааг утга дээрх оролт хэрэгтэй байсан (B2). Equivalent гэж үзэх мутант гараагүй нь "амьд мутант бол ихэвчлэн тест дутуу" гэсэн зааврын анхааруулгыг баталж байна. AI-гаар тест бичүүлэхэд гарах гол эрсдэл нь *хамралт өндөр, oracle сул* тест юм: ийм тест бүх мөрийг дайрч JaCoCo-г "ногоон" болгодог ч зан төлөвийг шалгадаггүй, эсвэл одоогийн кодын гаралтыг (алдаатай ч гэсэн) хуулж assertion болгодог. Иймд AI-ийн бичсэн тестийн чанарыг coverage-ээр биш mutation score-оор шалгаж, амьд мутант бүрийг спецификацитай тулгах нь тестийн багц бодитоор алдаа илрүүлж чадах эсэхийг батлах хамгийн шууд арга юм.
