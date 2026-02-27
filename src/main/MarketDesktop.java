package main;

import model.Product;
import model.PaymentMethod;
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

    // Interface components
    private DefaultTableModel productTableModel;
    private DefaultTableModel cartTableModel;
    private JLabel totalLabel;
    private JTextField searchField;

    public MarketDesktop() {
        setTitle("VTN POS");
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

        // Top section: Form and Search
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // Register Form
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formPanel.setBorder(BorderFactory.createTitledBorder("Cadastrar Novo Produto"));
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

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Buscar Produto"));
        searchField = new JTextField(20);
        searchPanel.add(new JLabel("Nome ou ID:"));
        searchPanel.add(searchField);

        topPanel.add(formPanel);
        topPanel.add(searchPanel);

        // Products Table (Center)
        String[] columns = {"ID", "Nome", "Preço", "Estoque"};
        productTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable productTable = new JTable(productTableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);

        // Add to cart Button
        JButton btnAddToCart = new JButton("Adicionar ao Carrinho");
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(btnAddToCart);

        // Actions
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

        // Search Action
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updateProductTable();
            }
        });

        btnAddToCart.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow != -1) {
                // Obter ID da coluna 0 da linha selecionada
                int id = (int) productTable.getValueAt(selectedRow, 0);
                Product p = findProductById(id);
                
                if (p != null) {
                    String response = JOptionPane.showInputDialog(this, "Quantidade de " + p.getName() + " que deseja adicionar:", "Adicionar ao Carrinho", JOptionPane.QUESTION_MESSAGE);
                    
                    if (response != null && !response.isEmpty()) {
                        try {
                            int requestedQuantity = Integer.parseInt(response);
                            if (requestedQuantity <= 0) {
                                JOptionPane.showMessageDialog(this, "A quantidade deve ser maior que zero!", "Erro", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            int qtdNoCarrinho = cart.getOrDefault(p, 0);
                            
                            if (p.getStock() >= (qtdNoCarrinho + requestedQuantity)) {
                                cart.put(p, qtdNoCarrinho + requestedQuantity);
                                updateCartTable();
                                JOptionPane.showMessageDialog(this, requestedQuantity + " " + p.getName() + "(s) adicionado(s) ao carrinho!");
                            } else {
                                JOptionPane.showMessageDialog(this, "Estoque insuficiente para este produto!\nQuantidade disponível: " + (p.getStock() - qtdNoCarrinho), "Erro", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this, "Quantidade inválida!", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecione um produto na lista.");
            }
        });

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }

    private Product findProductById(int id) {
        for (Product p : products) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Cart table
        String[] columns = {"ID", "Produto", "Preço Unit.", "Qtd", "Subtotal"};
        cartTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable cartTable = new JTable(cartTableModel);
        JScrollPane scrollPane = new JScrollPane(cartTable);

        // Finalization and resume
        JPanel footerPanel = new JPanel(new BorderLayout());
        totalLabel = new JLabel("Total: R$ 0,00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnRemove = new JButton("Remover Item");
        btnRemove.setBackground(new Color(220, 53, 69));
        btnRemove.setForeground(Color.WHITE);

        JButton btnFinish = new JButton("Finalizar Compra");
        btnFinish.setBackground(new Color(46, 139, 87));
        btnFinish.setForeground(Color.WHITE);

        actionsPanel.add(btnRemove);
        actionsPanel.add(btnFinish);

        footerPanel.add(totalLabel, BorderLayout.WEST);
        footerPanel.add(actionsPanel, BorderLayout.EAST);

        // Remove item action
        btnRemove.addActionListener(e -> {
            int selectedRow = cartTable.getSelectedRow();
            if (selectedRow != -1) {
                int id = (int) cartTable.getValueAt(selectedRow, 0);
                String name = (String) cartTable.getValueAt(selectedRow, 1);
                int currentQty = (int) cartTable.getValueAt(selectedRow, 3);
                
                Product p = null;
                for (Product cartProduct : cart.keySet()) {
                    if (cartProduct.getId() == id) {
                        p = cartProduct;
                        break;
                    }
                }
                
                if (p == null) return;

                if (currentQty > 1) {
                    String[] options = {"Remover Tudo", "Remover Quantidade Específica", "Cancelar"};
                    int choice = JOptionPane.showOptionDialog(this,
                            "O item '" + name + "' possui " + currentQty + " unidades no carrinho.\nO que deseja fazer?",
                            "Remover Item",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);

                    if (choice == 0) { // Remover Tudo
                        cart.remove(p);
                        updateCartTable();
                        JOptionPane.showMessageDialog(this, "Item '" + name + "' removido completamente.");
                    } else if (choice == 1) { // Quantidade Específica
                        String response = JOptionPane.showInputDialog(this, 
                                "Quantidade atual: " + currentQty + "\nQuantas unidades deseja remover?", 
                                "Remover Parcialmente", 
                                JOptionPane.QUESTION_MESSAGE);
                        
                        if (response != null && !response.isEmpty()) {
                            try {
                                int qtyToRemove = Integer.parseInt(response);
                                if (qtyToRemove <= 0) {
                                    JOptionPane.showMessageDialog(this, "A quantidade deve ser maior que zero!", "Erro", JOptionPane.ERROR_MESSAGE);
                                } else if (qtyToRemove >= currentQty) {
                                    cart.remove(p);
                                    updateCartTable();
                                    JOptionPane.showMessageDialog(this, "Todas as unidades de '" + name + "' foram removidas.");
                                } else {
                                    cart.put(p, currentQty - qtyToRemove);
                                    updateCartTable();
                                    JOptionPane.showMessageDialog(this, qtyToRemove + " unidade(s) de '" + name + "' removida(s).");
                                }
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(this, "Quantidade inválida!", "Erro", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                } else {
                    // Se houver apenas 1 unidade, remove direto sem perguntar
                    cart.remove(p);
                    updateCartTable();
                    JOptionPane.showMessageDialog(this, "Item '" + name + "' removido com sucesso!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecione um item no carrinho para remover.");
            }
        });

        // Finalization action
        btnFinish.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Carrinho vazio!");
                return;
            }
            double total = calculateTotal();

            // Select payment method
            PaymentMethod[] methods = PaymentMethod.values();
            PaymentMethod selectedMethod = (PaymentMethod) JOptionPane.showInputDialog(
                    this,
                    "Total da Venda: " + Utils.doubleToString(total) + "\n\nSelecione o método de pagamento:",
                    "Finalizar Venda",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    methods,
                    methods[0]
            );

            if (selectedMethod == null) {
                return; // User cancelled
            }
            
            // Decrement management
            for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
                Product p = entry.getKey();
                int qtdComprada = entry.getValue();
                p.setStock(p.getStock() - qtdComprada);
            }
            
            JOptionPane.showMessageDialog(this, 
                "Compra finalizada com sucesso!\n" +
                "Método de Pagamento: " + selectedMethod.getDescription() + "\n" +
                "Total: " + Utils.doubleToString(total),
                "Venda Concluída",
                JOptionPane.INFORMATION_MESSAGE);
                
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
        String query = (searchField != null) ? searchField.getText().toLowerCase().trim() : "";

        for (Product p : products) {
            boolean matches = true;
            if (!query.isEmpty()) {
                String idStr = String.valueOf(p.getId());
                String name = p.getName().toLowerCase();
                matches = idStr.contains(query) || name.contains(query);
            }

            if (matches) {
                productTableModel.addRow(new Object[]{p.getId(), p.getName(), Utils.doubleToString(p.getPrice()), p.getStock()});
            }
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
