package com.violeta.fia.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class PdfGenerator {

    private PdfGenerator() {
    }

    /**
     * Genera un PDF minimo con una sola pagina de texto sin dependencias externas.
     */
    public static byte[] generar(String titulo, List<String> lineas) {
        try {
            StringBuilder contenido = new StringBuilder();
            contenido.append("BT\n");
            contenido.append("/F1 16 Tf\n");
            contenido.append("50 800 Td\n");
            contenido.append("(").append(escapar(titulo)).append(") Tj\n");
            contenido.append("/F1 10 Tf\n");
            contenido.append("0 -26 Td\n");
            for (String linea : lineas) {
                contenido.append("(").append(escapar(linea)).append(") Tj\n");
                contenido.append("0 -14 Td\n");
            }
            contenido.append("ET\n");

            byte[] stream = contenido.toString().getBytes(StandardCharsets.US_ASCII);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<Integer> offsets = new ArrayList<>();

            escribir(out, "%PDF-1.4\n");

            offsets.add(out.size());
            escribir(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

            offsets.add(out.size());
            escribir(out, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

            offsets.add(out.size());
            escribir(out, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n");

            offsets.add(out.size());
            escribir(out, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

            offsets.add(out.size());
            escribir(out, "5 0 obj\n<< /Length " + stream.length + " >>\nstream\n");
            out.write(stream);
            escribir(out, "\nendstream\nendobj\n");

            int xrefOffset = out.size();
            int total = offsets.size() + 1;
            escribir(out, "xref\n0 " + total + "\n");
            escribir(out, "0000000000 65535 f \n");
            for (Integer offset : offsets) {
                escribir(out, String.format("%010d 00000 n \n", offset));
            }
            escribir(out, "trailer\n<< /Size " + total + " /Root 1 0 R >>\n");
            escribir(out, "startxref\n" + xrefOffset + "\n%%EOF\n");

            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el PDF", e);
        }
    }

    private static void escribir(ByteArrayOutputStream out, String texto) throws IOException {
        out.write(texto.getBytes(StandardCharsets.US_ASCII));
    }

    private static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == '\\' || c == '(' || c == ')') {
                sb.append('\\').append(c);
            } else if (c < 32 || c > 126) {
                sb.append('?');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
