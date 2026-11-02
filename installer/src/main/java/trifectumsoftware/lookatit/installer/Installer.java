package trifectumsoftware.lookatit.installer;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public final class Installer {
    private static final String[] VERSIONS = {
            "1.7.10 (Forge)",
            "1.8.9 (Forge)",
            "1.12.2 (Forge)",
            "1.20.1 (Forge)",
            "1.20.1 (Fabric)",
            "1.21.1 (NeoForge)",
            "26.1.2 (NeoForge)",
            "26.2 (NeoForge)",
            "26.3 (NeoForge)"
    };

    private static final String[] FILES = {
            "lookatit-1.7.10-Forge.jar",
            "lookatit-1.8.9-Forge.jar",
            "lookatit-1.12.2-Forge.jar",
            "lookatit-1.20.1-Forge.jar",
            "lookatit-1.20.1-Fabric.jar",
            "lookatit-1.21.1-NeoForge.jar",
            "lookatit-26.1.2-NeoForge.jar",
            "lookatit-26.2-NeoForge.jar",
            "lookatit-26.3-NeoForge.jar"
    };

    private Installer() {
    }

    public static void main(String[] args) {
        if (args.length >= 2) {
            String version = args[0];
            Path mods = Paths.get(args[1]);
            try {
                install(version, mods);
                System.out.println("Installed lookatit for " + version + " into " + mods);
            } catch (Exception e) {
                System.err.println("Install failed: " + e.getMessage());
                System.exit(1);
            }
            return;
        }
        SwingUtilities.invokeLater(Installer::showGui);
    }

    private static void showGui() {
        JFrame frame = new JFrame("lookatit installer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JComboBox<String> versions = new JComboBox<>(VERSIONS);
        JTextField mods = new JTextField(defaultModsFolder().toString());
        JLabel status = new JLabel("Choose your Minecraft version and mods folder.");

        JButton install = new JButton("Install");
        install.addActionListener(e -> {
            try {
                String version = (String) versions.getSelectedItem();
                Path target = Paths.get(mods.getText().trim());
                install(version, target);
                status.setText("Installed " + version + " into " + target);
                JOptionPane.showMessageDialog(frame,
                        "Installed lookatit for " + version + ".\n\n" + target,
                        "lookatit", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                status.setText("Failed: " + ex.getMessage());
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "lookatit", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("Minecraft version / loader"));
        form.add(versions);
        form.add(new JLabel("Mods folder"));
        form.add(mods);
        form.add(install);

        JPanel root = new JPanel(new BorderLayout());
        root.add(form, BorderLayout.CENTER);
        root.add(status, BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void install(String version, Path modsFolder) throws IOException {
        int index = -1;
        for (int i = 0; i < VERSIONS.length; i++) {
            if (VERSIONS[i].equals(version)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new IllegalArgumentException("Unknown version: " + version);
        }
        String file = FILES[index];
        try (InputStream in = Installer.class.getResourceAsStream("/mods/" + file)) {
            if (in == null) {
                throw new IOException("Bundled mod jar not found: " + file);
            }
            Files.createDirectories(modsFolder);
            Files.copy(in, modsFolder.resolve(file), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Path defaultModsFolder() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home");
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isEmpty()) {
                return Paths.get(appData, ".minecraft", "mods");
            }
        } else if (os.contains("mac")) {
            return Paths.get(home, "Library", "Application Support", "minecraft", "mods");
        }
        return Paths.get(home, ".minecraft", "mods");
    }
}
