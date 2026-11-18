package br.ufpb.aps.granja.model;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

/**
 * CSV pro Excel em portugues: separador ponto e virgula, UTF-8 com BOM e fim de linha CRLF.
 * Celula de texto que comeca com = + - @ ganha um apostrofo na frente, senao o Excel a executa como formula.
 */
public final class Csv {

    private static final byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final Pattern NUMERO = Pattern.compile("-?\\d+([.,]\\d+)?");

    private Csv() {
    }

    public static byte[] montar(TabelaExportada tabela) {
        StringBuilder texto = new StringBuilder();
        linha(texto, tabela.cabecalho());
        tabela.linhas().forEach(l -> linha(texto, l));
        byte[] corpo = texto.toString().getBytes(StandardCharsets.UTF_8);
        byte[] saida = new byte[BOM.length + corpo.length];
        System.arraycopy(BOM, 0, saida, 0, BOM.length);
        System.arraycopy(corpo, 0, saida, BOM.length, corpo.length);
        return saida;
    }

    private static void linha(StringBuilder texto, List<String> celulas) {
        for (int i = 0; i < celulas.size(); i++) {
            if (i > 0) {
                texto.append(';');
            }
            texto.append(celula(celulas.get(i)));
        }
        texto.append("\r\n");
    }

    static String celula(String bruto) {
        String valor = bruto == null ? "" : bruto;
        if (!valor.isEmpty() && "=+-@\t\r".indexOf(valor.charAt(0)) >= 0 && !NUMERO.matcher(valor).matches()) {
            valor = "'" + valor;
        }
        boolean precisaAspas = valor.contains(";") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r");
        return precisaAspas ? "\"" + valor.replace("\"", "\"\"") + "\"" : valor;
    }
}
