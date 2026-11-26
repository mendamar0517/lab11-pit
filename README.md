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
