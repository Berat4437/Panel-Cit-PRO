import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class PanelHesaplama {

    static DrawPanel drawPanel = new DrawPanel();
    static double lastKg = 0, lastCap = 0, lastL = 0, lastH = 0, lastGy = 0, lastGx = 0;
    static int lastYatay = 0, lastDikey = 0;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Panel Çit PRO");
        frame.setSize(1250, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel left = new JPanel(new GridBagLayout());
        left.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        left.setBackground(new Color(250, 250, 250));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 10, 10, 10);
        c.gridx = 0; c.gridy = 0;

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Kutucuklar boş hale getirildi
        JTextField txtL = new JTextField();
        JTextField txtH = new JTextField();
        JTextField txtGy = new JTextField();
        JTextField txtGx = new JTextField();
        JTextField txtCap = new JTextField();

        setupEnterAction(txtL, txtH);
        setupEnterAction(txtH, txtGy);
        setupEnterAction(txtGy, txtGx);
        setupEnterAction(txtGx, txtCap);

        JButton hesapla = createButton("HESAPLA");
        JButton infoBtn = createButton("Panel Çit Bilgileri");
        JButton pngBtn = createButton("PNG KAYDET");

        txtCap.addActionListener(e -> hesapla.doClick());

        addField(left, c, "Genişlik (m)", txtL, labelFont);
        addField(left, c, "Yükseklik (m)", txtH, labelFont);
        addField(left, c, "Yatay göz (mm)", txtGy, labelFont);
        addField(left, c, "Dikey göz (mm)", txtGx, labelFont);
        addField(left, c, "Tel çapı (mm)", txtCap, labelFont);

        left.add(hesapla, c); c.gridy++;
        left.add(infoBtn, c); c.gridy++;
        left.add(pngBtn, c);

        frame.add(left, BorderLayout.WEST);
        frame.add(drawPanel, BorderLayout.CENTER);

        hesapla.addActionListener(e -> {
            try {
                double L = Double.parseDouble(txtL.getText().replace(",", "."));
                double H = Double.parseDouble(txtH.getText().replace(",", "."));
                double gy_in = Double.parseDouble(txtGy.getText().replace(",", "."));
                double gx_in = Double.parseDouble(txtGx.getText().replace(",", "."));
                double cap = Double.parseDouble(txtCap.getText().replace(",", "."));

                double gy_m = gy_in / 1000.0;
                double gx_m = gx_in / 1000.0;

                int dikey = Math.max(2, (int) Math.round(L / gy_m) + 1);
                int yatay = Math.max(2, (int) Math.round(H / gx_m) + 1);

                lastL = L; lastH = H; lastGy = gy_in; lastGx = gx_in;
                lastYatay = yatay; lastDikey = dikey; lastCap = cap;
                lastKg = ((yatay * L) + (dikey * H)) * (0.00617 * cap * cap);

                drawPanel.setValues(L, H, yatay, dikey, lastGy, lastGx);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Hata: Sayısal değerleri kontrol edin.");
            }
        });

        infoBtn.addActionListener(e -> {
            if (lastL == 0) {
                JOptionPane.showMessageDialog(frame, "Önce hesaplama yapmalısınız!");
                return;
            }
            JOptionPane.showMessageDialog(frame,
                    "PANEL ÇİT BİLGİLERİ\n\n" +
                            "Genişlik: " + lastL + " m\n" +
                            "Yükseklik: " + lastH + " m\n" +
                            "Yatay tel adedi: " + lastYatay + "\n" +
                            "Dikey tel adedi: " + lastDikey + "\n" +
                            "Göz: " + (int)lastGy + " x " + (int)lastGx + " mm\n" +
                            "Tel Çapı: " + lastCap + " mm\n" +
                            "Tahmini Ağırlık: " + String.format("%.2f", lastKg) + " kg"
            );
        });

        pngBtn.addActionListener(e -> {
            try {
                BufferedImage img = new BufferedImage(drawPanel.getWidth(), drawPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = img.createGraphics();
                drawPanel.paint(g2);
                g2.dispose();
                ImageIO.write(img, "png", new File(System.getProperty("user.home") + "/Desktop/panel_cizim.png"));
                JOptionPane.showMessageDialog(frame, "Masaüstüne kaydedildi!");
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        frame.setVisible(true);
    }

    private static void setupEnterAction(JTextField current, JTextField next) {
        current.addActionListener(e -> next.requestFocusInWindow());
    }

    static JButton createButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(new Color(230, 230, 230));
        b.setFocusPainted(false);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(60, 200, 120)); b.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { b.setBackground(new Color(230, 230, 230)); b.setForeground(Color.BLACK); }
        });
        return b;
    }

    static void addField(JPanel p, GridBagConstraints c, String label, JTextField f, Font font) {
        JLabel l = new JLabel(label); l.setFont(font);
        p.add(l, c); c.gridy++;
        f.setPreferredSize(new Dimension(180, 28));
        p.add(f, c); c.gridy++;
    }

    static class DrawPanel extends JPanel {
        double L, H, gy, gx; int yatay, dikey; boolean valuesSet = false;

        public void setValues(double L, double H, int y, int d, double gy, double gx) {
            this.L = L; this.H = H; this.yatay = y; this.dikey = d; this.gy = gy; this.gx = gx;
            this.valuesSet = true; repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE); g2.fillRect(0, 0, getWidth(), getHeight());

            if (!valuesSet) return;

            int margin = 120;
            int availableW = getWidth() - 2 * margin;
            int availableH = getHeight() - 2 * margin;
            double aspectInput = L / H;
            double aspectCanvas = (double) availableW / availableH;

            int w, h;
            if (aspectInput > aspectCanvas) {
                w = availableW;
                h = (int) (w / aspectInput);
            } else {
                h = availableH;
                w = (int) (h * aspectInput);
            }

            int x = (getWidth() - w) / 2;
            int y = (getHeight() - h) / 2;

            g2.setStroke(new BasicStroke(3.5f)); g2.setColor(new Color(40, 40, 40));
            g2.drawRect(x, y, w, h);

            double stepW = (double) w / (dikey - 1);
            double stepH = (double) h / (yatay - 1);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            drawBrace(g2, x, y - 40, w, true, String.format("%.0f cm", L * 100), Color.BLACK);
            drawBrace(g2, x - 40, y, h, false, String.format("%.0f cm", H * 100), Color.BLACK);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(new Color(0, 102, 204));
            g2.drawString("Göz Aralığı: " + (int)gy + "x" + (int)gx + " mm", x, y + h + 30);

            g2.setColor(new Color(100, 100, 100));
            g2.setStroke(new BasicStroke(1.5f));

            for (int i = 1; i < yatay - 1; i++) {
                int py = y + (int)Math.round(i * stepH);
                g2.drawLine(x, py, x + w, py);
            }
            for (int i = 1; i < dikey - 1; i++) {
                int px = x + (int)Math.round(i * stepW);
                g2.drawLine(px, y, px, y + h);
            }
        }

        void drawBrace(Graphics2D g2, int x, int y, int len, boolean horiz, String text, Color color) {
            AffineTransform old = g2.getTransform(); g2.setColor(color);
            FontMetrics fm = g2.getFontMetrics();
            if (horiz) {
                g2.drawLine(x, y + 10, x + len, y + 10);
                g2.drawLine(x, y + 5, x, y + 15); g2.drawLine(x + len, y + 5, x + len, y + 15);
                g2.drawString(text, x + (len / 2) - (fm.stringWidth(text) / 2), y);
            } else {
                g2.translate(x + 10, y + len / 2); g2.rotate(-Math.PI / 2);
                g2.drawLine(-len / 2, 0, len / 2, 0);
                g2.drawLine(-len / 2, -5, -len / 2, 5); g2.drawLine(len / 2, -5, len / 2, 5);
                g2.drawString(text, -fm.stringWidth(text) / 2, -10);
            }
            g2.setTransform(old);
        }
    }
}
