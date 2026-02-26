package main;

import model.Product;
import utils.Utils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MarketDesktop extends JFrame {
    private ArrayList<Product> products = new ArrayList<>();
    private Map<Product, Integer> cart = new HashMap<>();

    // Componentes da Interface
    private DefaultTableModel productTableModel;
    private DefaultTableModel cartTableModel;
    private JLabel totalLabel;

    public MarketDesktop() {
        setTitle("Market PDV - Sistema Desktop");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Abas
        tabbedPane.addTab("Cadastrar/Listar", createProductPanel());
        tabbedPane.addTab("Carrinho de Compras", createCartPanel());

        add(tabbedPane);
    }

    private JPanel createProductPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Formulário de Cadastro (Norte)
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField nameField = new JTextField(15);
        JTextField priceField = new JTextField(10);
        JTextField stockField = new JTextField(5);
        JButton btnAdd = new JButton("Cadastrar");

        formPanel.add(new JLabel("Nome:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Preço:"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Qtd Estoque:"));
        formPanel.add(stockField);
        formPanel.add(btnAdd);

        // Tabela de Produtos (Centro)
        String[] columns = {"ID", "Nome", "Preço", "Estoque"};
        productTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable productTable = new JTable(productTableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);

        // Botão Adicionar ao Carrinho (Sul)
        JButton btnAddToCart = new JButton("Adicionar ao Carrinho");
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(btnAddToCart);

        // Ações
        btnAdd.addActionListener(e -> {
            try {
                String name = nameField.getText();
                Double price = Double.parseDouble(priceField.getText().replace(",", "."));
                int stock = Integer.parseInt(stockField.getText());
                Product p = new Product(name, price, stock);
                products.add(p);
                updateProductTable();
                nameField.setText("");
                priceField.setText("");
                stockField.setText("");
                JOptionPane.showMessageDialog(this, "Produto cadastrado!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Preço ou Estoque inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnAddToCart.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow != -1) {
                Product p = products.get(selectedRow);
                int qtdNoCarrinho = cart.getOrDefault(p, 0);
                
                if (p.getStock() > qtdNoCarrinho) {
                    cart.put(p, qtdNoCarrinho + 1);
                    updateCartTable();
                    JOptionPane.showMessageDialog(this, p.getName() + " adicionado ao carrinho!");
                } else {
                    JOptionPane.showMessageDialog(this, "Estoque insuficiente para este produto!", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecione um produto na lista.");
            }
        });

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tabela do Carrinho
        String[] columns = {"ID", "Produto", "Preço Unit.", "Qtd", "Subtotal"};
        cartTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable cartTable = new JTable(cartTableModel);
        JScrollPane scrollPane = new JScrollPane(cartTable);

        // Resumo e Finalização (Sul)
        JPanel footerPanel = new JPanel(new BorderLayout());
        totalLabel = new JLabel("Total: R$ 0,00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        JButton btnFinish = new JButton("Finalizar Compra");
        btnFinish.setBackground(new Color(46, 139, 87));
        btnFinish.setForeground(Color.WHITE);

        footerPanel.add(totalLabel, BorderLayout.WEST);
        footerPanel.add(btnFinish, BorderLayout.EAST);

        // Ação Finalizar
        btnFinish.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Carrinho vazio!");
                return;
            }
            double total = calculateTotal();
            
            // Decrementar estoque
            for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
                Product p = entry.getKey();
                int qtdComprada = entry.getValue();
                p.setStock(p.getStock() - qtdComprada);
            }
            
            JOptionPane.showMessageDialog(this, "Compra finalizada!\nTotal: " + Utils.doubleToString(total));
            cart.clear();
            updateCartTable();
            updateProductTable(); // Atualiza a lista de produtos com o novo estoque
        });

        panel.add(new JLabel("Itens no Carrinho:"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateProductTable() {
        productTableModel.setRowCount(0);
        for (Product p : products) {
            productTableModel.addRow(new Object[]{p.getId(), p.getName(), Utils.doubleToString(p.getPrice()), p.getStock()});
        }
    }

    private void updateCartTable() {
        cartTableModel.setRowCount(0);
        double total = 0;
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            Product p = entry.getKey();
            int qtd = entry.getValue();
            double subtotal = p.getPrice() * qtd;
            total += subtotal;
            cartTableModel.addRow(new Object[]{
                    p.getId(), p.getName(), Utils.doubleToString(p.getPrice()), qtd, Utils.doubleToString(subtotal)
            });
        }
        totalLabel.setText("Total: " + Utils.doubleToString(total));
    }

    private double calculateTotal() {
        double total = 0;
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            total += entry.getKey().getPrice() * entry.getValue();
        }
        return total;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MarketDesktop().setVisible(true);
        });
    }
}
