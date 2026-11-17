package br.ufpb.aps.granja.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RN14: CPF e CNPJ conferidos pelos digitos verificadores. */
class DocumentoTest {

    @Test
    void cpfComOuSemPontuacaoViraOMesmoDocumento() {
        Documento comMascara = Documento.de("529.982.247-25");
        Documento semMascara = Documento.de("52998224725");

        assertEquals(semMascara, comMascara);
        assertTrue(comMascara.ehCpf());
    }

    @Test
    void cnpjValidoEhAceito() {
        Documento cnpj = Documento.de("11.222.333/0001-81");

        assertEquals("11222333000181", cnpj.numero());
        assertFalse(cnpj.ehCpf());
    }

    @Test
    void digitoVerificadorErradoEhRecusado() {
        assertThrows(IllegalArgumentException.class, () -> Documento.de("529.982.247-26"));
        assertThrows(IllegalArgumentException.class, () -> Documento.de("11.222.333/0001-80"));
    }

    @Test
    void todosOsDigitosIguaisOuTamanhoErradoSaoRecusados() {
        assertThrows(IllegalArgumentException.class, () -> Documento.de("111.111.111-11"));
        assertThrows(IllegalArgumentException.class, () -> Documento.de("1234567890"));
        assertThrows(IllegalArgumentException.class, () -> Documento.de("abc"));
        assertThrows(IllegalArgumentException.class, () -> Documento.de(null));
    }
}
