package main;

import model.Product;
import model.PaymentMethod;
import model.Sale;
import utils.Utils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MarketDesktop extends JFrame {
    private ArrayList<Product> products = new ArrayList<>();
    private Map<Product, Integer> cart = new HashMap<>();
    private ArrayList<Sale> sales = new ArrayList<>();

    // Interface components
    private DefaultTableModel productTableModel;
    private DefaultTableModel cartTableModel;
    private DefaultTableModel salesTableModel;
    private JLabel totalLabel;
    private JLabel totalSalesLabel;
    private JTextField searchField;
    
    // Filtros de data
    private JTextField startDateSalesField;
    private JTextField endDateSalesField;
    private JTextField startDateCartField;
    private JTextField endDateCartField;

    public MarketDesktop() {
        setTitle("VTN POS");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Abas
        tabbedPane.addTab("Cadastrar/Listar", createProductPanel());
        tabbedPane.addTab("Efetuar Venda", createCartPanel());
        tabbedPane.addTab("Gerenciamento de Entradas", createSalesPanel());

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
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- TOP: Cart Section ---
        JPanel cartPanel = new JPanel(new BorderLayout(5, 5));
        cartPanel.setBorder(BorderFactory.createTitledBorder("Carrinho Atual"));

        // Cart table
        String[] columns = {"ID", "Produto", "Preço Unit.", "Qtd", "Subtotal"};
        cartTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable cartTable = new JTable(cartTableModel);
        JScrollPane cartScrollPane = new JScrollPane(cartTable);

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

        cartPanel.add(cartScrollPane, BorderLayout.CENTER);
        cartPanel.add(footerPanel, BorderLayout.SOUTH);

        // --- BOTTOM: Quick History Section ---
        JPanel historyPanel = new JPanel(new BorderLayout(5, 5));
        historyPanel.setBorder(BorderFactory.createTitledBorder("Vendas Recentes"));

        // Filter for quick history
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startDateCartField = new JTextField(8);
        endDateCartField = new JTextField(8);
        JButton btnFilterCart = new JButton("Filtrar");
        JButton btnClearCart = new JButton("Limpar");
        
        filterPanel.add(new JLabel("De:"));
        filterPanel.add(startDateCartField);
        filterPanel.add(new JLabel("Até:"));
        filterPanel.add(endDateCartField);
        filterPanel.add(btnFilterCart);
        filterPanel.add(btnClearCart);

        String[] historyColumns = {"DATA", "TOTAL", "PAGAMENTO"};
        DefaultTableModel quickSalesTableModel = new DefaultTableModel(historyColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable quickSalesTable = new JTable(quickSalesTableModel);
        JScrollPane historyScrollPane = new JScrollPane(quickSalesTable);
        historyScrollPane.setPreferredSize(new Dimension(0, 150));

        historyPanel.add(filterPanel, BorderLayout.NORTH);
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cartPanel, historyPanel);
        splitPane.setResizeWeight(0.7);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Actions
        btnFilterCart.addActionListener(e -> updateQuickSalesTable(quickSalesTableModel));
        btnClearCart.addActionListener(e -> {
            startDateCartField.setText("");
            endDateCartField.setText("");
            updateQuickSalesTable(quickSalesTableModel);
        });
        
        // Inicializar tabela de vendas recentes
        updateQuickSalesTable(quickSalesTableModel);

        // ... (Keep existing actions for btnRemove and btnFinish)
        
        // --- Actions Implementation (Existing) ---
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
                    cart.remove(p);
                    updateCartTable();
                    JOptionPane.showMessageDialog(this, "Item '" + name + "' removido com sucesso!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecione um item no carrinho para remover.");
            }
        });

        btnFinish.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Carrinho vazio!");
                return;
            }
            double total = calculateTotal();

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
                return;
            }
            
            for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
                Product p = entry.getKey();
                int qtdComprada = entry.getValue();
                p.setStock(p.getStock() - qtdComprada);
            }

            Sale sale = new Sale(new HashMap<>(cart), total, selectedMethod);
            sales.add(sale);
            updateSalesTable();
            updateQuickSalesTable(quickSalesTableModel); // Update the quick history table

            JOptionPane.showMessageDialog(this, 
                "Compra finalizada com sucesso!\n" +
                "Método de Pagamento: " + selectedMethod.getDescription() + "\n" +
                "Total: " + Utils.doubleToString(total),
                "Venda Concluída",
                JOptionPane.INFORMATION_MESSAGE);
                
            cart.clear();
            updateCartTable();
            updateProductTable();
        });

        return mainPanel;
    }

    private void updateQuickSalesTable(DefaultTableModel model) {
        if (model == null) return;
        model.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate startDate = null;
        LocalDate endDate = null;

        try {
            if (startDateCartField != null && !startDateCartField.getText().trim().isEmpty()) {
                startDate = LocalDate.parse(startDateCartField.getText().trim(), dateOnlyFormatter);
            }
            if (endDateCartField != null && !endDateCartField.getText().trim().isEmpty()) {
                endDate = LocalDate.parse(endDateCartField.getText().trim(), dateOnlyFormatter);
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de data inválido! Use dd/mm/aaaa", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (Sale sale : sales) {
            LocalDate saleDate = sale.getDateTime().toLocalDate();
            boolean matches = true;

            if (startDate != null && saleDate.isBefore(startDate)) matches = false;
            if (endDate != null && saleDate.isAfter(endDate)) matches = false;

            if (matches) {
                model.addRow(new Object[]{
                        sale.getDateTime().format(formatter),
                        Utils.doubleToString(sale.getTotalValue()),
                        sale.getPaymentMethod().getDescription()
                });
            }
        }
    }

    private JPanel createSalesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtrar por Data"));
        
        startDateSalesField = new JTextField(10);
        endDateSalesField = new JTextField(10);
        JButton btnFilter = new JButton("Filtrar");
        JButton btnClear = new JButton("Limpar");

        filterPanel.add(new JLabel("Início (dd/mm/aaaa):"));
        filterPanel.add(startDateSalesField);
        filterPanel.add(new JLabel("Fim (dd/mm/aaaa):"));
        filterPanel.add(endDateSalesField);
        filterPanel.add(btnFilter);
        filterPanel.add(btnClear);

        // Sales table
        String[] columns = {"PRODUTO (S)", "VALOR TOTAL", "MÉTODO DE PAGAMENTO", "DATA"};
        salesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable salesTable = new JTable(salesTableModel);
        JScrollPane scrollPane = new JScrollPane(salesTable);

        // Summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalSalesLabel = new JLabel("Total Acumulado em Vendas: R$ 0,00");
        totalSalesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        summaryPanel.add(totalSalesLabel);

        // Actions
        btnFilter.addActionListener(e -> updateSalesTable());
        btnClear.addActionListener(e -> {
            startDateSalesField.setText("");
            endDateSalesField.setText("");
            updateSalesTable();
        });

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(new JLabel("Histórico de Vendas (Saídas):"), BorderLayout.NORTH);
        northPanel.add(filterPanel, BorderLayout.CENTER);

        panel.add(northPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(summaryPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateSalesTable() {
        if (salesTableModel == null) return;
        salesTableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        double accumulatedTotal = 0;

        LocalDate startDate = null;
        LocalDate endDate = null;

        try {
            if (startDateSalesField != null && !startDateSalesField.getText().trim().isEmpty()) {
                startDate = LocalDate.parse(startDateSalesField.getText().trim(), dateOnlyFormatter);
            }
            if (endDateSalesField != null && !endDateSalesField.getText().trim().isEmpty()) {
                endDate = LocalDate.parse(endDateSalesField.getText().trim(), dateOnlyFormatter);
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de data inválido! Use dd/mm/aaaa", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (Sale sale : sales) {
            LocalDate saleDate = sale.getDateTime().toLocalDate();
            boolean matches = true;

            if (startDate != null && saleDate.isBefore(startDate)) {
                matches = false;
            }
            if (endDate != null && saleDate.isAfter(endDate)) {
                matches = false;
            }

            if (matches) {
                accumulatedTotal += sale.getTotalValue();
                salesTableModel.addRow(new Object[]{
                        sale.getProductsSummary(),
                        Utils.doubleToString(sale.getTotalValue()),
                        sale.getPaymentMethod().getDescription(),
                        sale.getDateTime().format(formatter)
                });
            }
        }
        totalSalesLabel.setText("Total Acumulado em Vendas: " + Utils.doubleToString(accumulatedTotal));
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
