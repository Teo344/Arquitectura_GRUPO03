package ec.edu.monster.modelo.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PaisDAOMySQLTest {

    @Test
    void escapaLosComodinesDeLike() {
        assertEquals("100\\%", PaisDAOMySQL.escaparLike("100%"));
        assertEquals("a\\_b", PaisDAOMySQL.escaparLike("a_b"));
        assertEquals("c\\\\d", PaisDAOMySQL.escaparLike("c\\d"));
        assertEquals("Ecuador", PaisDAOMySQL.escaparLike("Ecuador"));
    }
}
