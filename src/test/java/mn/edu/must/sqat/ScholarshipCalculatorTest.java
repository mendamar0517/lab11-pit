package mn.edu.must.sqat;

// ЛАБ 11 — B1: Assertion-уудыг хүчтэй болгосон хувилбар.
// Оролтууд Starter-ийнхтэй ЯГ АДИЛ; зөвхөн assertNotNull / assertTrue(муж)
// шалгалтуудыг спецификацийн (oracle) ЯГ утгын assertEquals болгосон.

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
