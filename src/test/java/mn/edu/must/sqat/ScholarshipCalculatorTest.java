package mn.edu.must.sqat;

// ЛАБ 11 — хүчтэй болгосон тестийн багц.
// B1: assertNotNull / assertTrue(муж) шалгалтуудыг спецификацийн (oracle)
//     ЯГ утгын assertEquals болгосон (оролтууд Starter-ийнхтэй адил).
// B2: зааг утгын (boundary) тестүүд нэмсэн — letterGrade-ийн 0/60/70/80/90/100,
//     scholarship-ийн 3.8/15, 3.4/12, 3.0 заагууд, оролтын хязгаар
//     (gpa 0 ба 4, credits 0) болон gpa-гийн switch-ийн бүх үсгэн дүн.

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ScholarshipCalculatorTest {

    private static final double EPS = 1e-9;

    ScholarshipCalculator calc;

    @BeforeEach
    void setUp() { calc = new ScholarshipCalculator(); }

    // ---- letterGrade: муж бүрт ЯГ үсгэн дүн ----

    @Test
    void letterGrade_returnsExactLetterForEachRange() {
        assertEquals("A", calc.letterGrade(95));
        assertEquals("B", calc.letterGrade(85));
        assertEquals("C", calc.letterGrade(75));
        assertEquals("D", calc.letterGrade(65));
        assertEquals("F", calc.letterGrade(30));
    }

    @Test
    void letterGrade_invalidInputs_throw() {
        assertThrows(IllegalArgumentException.class, () -> calc.letterGrade(-1));
        assertThrows(IllegalArgumentException.class, () -> calc.letterGrade(101));
    }

    // ---- gpa: ЯГ голч (A=4, B=3, C=2, D=1, F=0) ----

    @Test
    void gpa_returnsExactAverage() {
        assertEquals(3.5, calc.gpa(new double[]{95, 85}), EPS); // (4 + 3) / 2
        assertEquals(1.5, calc.gpa(new double[]{75, 65}), EPS); // (2 + 1) / 2
        assertEquals(0.0, calc.gpa(new double[]{40}), EPS);     // F
    }

    @Test
    void gpa_invalidInputs_throw() {
        assertThrows(IllegalArgumentException.class, () -> calc.gpa(null));
        assertThrows(IllegalArgumentException.class, () -> calc.gpa(new double[]{}));
    }

    // ---- scholarship: дүрэм бүрт ЯГ хувь ----

    @Test
    void scholarship_returnsExactPercentForEachRule() {
        assertEquals(100, calc.scholarship(3.9, 16, false)); // >= 3.8 ба >= 15
        assertEquals(70,  calc.scholarship(3.5, 13, false)); // >= 3.4 ба >= 12
        assertEquals(50,  calc.scholarship(3.1, 5, false));  // >= 3.0
        assertEquals(0,   calc.scholarship(2.0, 1, false));  // бусад
        assertEquals(0,   calc.scholarship(4.0, 20, true));  // сахилгын зөрчил
    }

    @Test
    void scholarship_invalidInputs_throw() {
        assertThrows(IllegalArgumentException.class, () -> calc.scholarship(4.1, 10, false));
        assertThrows(IllegalArgumentException.class, () -> calc.scholarship(3.0, -1, false));
    }

    // =====================================================================
    // B2 — Зааг утгын тестүүд
    // =====================================================================

    // ---- letterGrade: үсэг бүрийн зааг ба оролтын хязгаар ----

    @ParameterizedTest(name = "letterGrade({0}) = {1}")
    @CsvSource({
            "100,   A",   // дээд хязгаар (score > 100 → >= 100 мутантыг алах)
            "90,    A",   // A-ийн доод зааг
            "89.99, B",
            "80,    B",   // B-ийн доод зааг
            "79.99, C",
            "70,    C",   // C-ийн доод зааг
            "69.99, D",
            "60,    D",   // D-ийн доод зааг
            "59.99, F",
            "0,     F"    // доод хязгаар (score < 0 → <= 0 мутантыг алах)
    })
    void letterGrade_boundaries(double score, String expected) {
        assertEquals(expected, calc.letterGrade(score));
    }

    @Test
    void letterGrade_justOutsideRange_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.letterGrade(-0.01));
        assertThrows(IllegalArgumentException.class, () -> calc.letterGrade(100.01));
    }

    // ---- gpa: switch-ийн бүх үсгэн дүн, хязгаарын утгууд ----

    @Test
    void gpa_eachLetterContributesExactPoints() {
        assertEquals(4.0, calc.gpa(new double[]{95}), EPS); // A
        assertEquals(3.0, calc.gpa(new double[]{85}), EPS); // B
        assertEquals(2.0, calc.gpa(new double[]{75}), EPS); // C
        assertEquals(1.0, calc.gpa(new double[]{65}), EPS); // D
        assertEquals(0.0, calc.gpa(new double[]{30}), EPS); // F (default)
    }

    @Test
    void gpa_allLettersTogether() {
        // (4 + 3 + 2 + 1 + 0) / 5 = 2.0
        assertEquals(2.0, calc.gpa(new double[]{95, 85, 75, 65, 30}), EPS);
    }

    @Test
    void gpa_boundaryScoresMapToCorrectLetters() {
        // 90→A(4), 80→B(3), 70→C(2), 60→D(1) → 10/4 = 2.5
        assertEquals(2.5, calc.gpa(new double[]{90, 80, 70, 60}), EPS);
        // 89.99→B(3), 79.99→C(2), 69.99→D(1), 59.99→F(0) → 6/4 = 1.5
        assertEquals(1.5, calc.gpa(new double[]{89.99, 79.99, 69.99, 59.99}), EPS);
        assertEquals(4.0, calc.gpa(new double[]{100, 100}), EPS);
        assertEquals(0.0, calc.gpa(new double[]{0, 0}), EPS);
    }

    @Test
    void gpa_invalidScoreInside_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.gpa(new double[]{95, 101}));
    }

    // ---- scholarship: дүрэм бүрийн зааг ----

    @ParameterizedTest(name = "scholarship({0}, {1}, false) = {2}")
    @CsvSource({
            // 100%-ийн зааг: gpa >= 3.8 ба credits >= 15
            "3.8,  15, 100",
            "3.79, 15, 70",    // gpa зааг доор → 70
            "3.8,  14, 70",    // credits зааг доор → 70
            // 70%-ийн зааг: gpa >= 3.4 ба credits >= 12
            "3.4,  12, 70",
            "3.39, 12, 50",    // gpa зааг доор → 50
            "3.4,  11, 50",    // credits зааг доор → 50
            // 50%-ийн зааг: gpa >= 3.0
            "3.0,  0,  50",
            "2.99, 20, 0",
            // оролтын хязгаар (хүчинтэй)
            "0.0,  0,  0",     // gpa = 0, credits = 0
            "4.0,  15, 100",   // gpa = 4
            "4.0,  0,  50"     // gpa = 4 ч кредит 0
    })
    void scholarship_boundaries(double gpa, int credits, int expected) {
        assertEquals(expected, calc.scholarship(gpa, credits, false));
    }

    @Test
    void scholarship_violationAlwaysZero_evenAtTop() {
        assertEquals(0, calc.scholarship(3.8, 15, true));
        assertEquals(0, calc.scholarship(3.0, 0, true));
    }

    @Test
    void scholarship_justOutsideInputLimits_throws() {
        assertThrows(IllegalArgumentException.class, () -> calc.scholarship(-0.01, 10, false));
        assertThrows(IllegalArgumentException.class, () -> calc.scholarship(4.01, 10, false));
        assertThrows(IllegalArgumentException.class, () -> calc.scholarship(3.0, -1, false));
    }
}
