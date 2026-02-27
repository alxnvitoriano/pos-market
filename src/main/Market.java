package main;

import model.Product;
import model.PaymentMethod;
import utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Market {
    private static Scanner input = new Scanner(System.in);
    private static ArrayList<Product> products;
    private static Map<Product, Integer> cart;

    static void main(String[] args) {
        products = new ArrayList<>();
        cart = new HashMap<>();
        menu();
    }

    private static void menu() {
        System.out.println("1 - CADASTRAR");
        System.out.println("2 - LISTAR");
        System.out.println("3 - COMPRAR");
        System.out.println("4 - CARRINHO");
        System.out.println("5 - SAIR");

        int option = input.nextInt();

        switch (option) {
            case 1:
                registerProduct();
                break;
            case 2:
                listProducts();
                break;
            case 3:
                buyProducts();
                break;
            case 4:
                seeCart();
                break;
            case 5:
                System.out.println("Volte Sempre!");
                System.exit(0);
            default:
                System.out.println("Opção Inválida.");
                menu();
                break;
        }
    }

    private static void registerProduct () {
        System.out.println("Nome do produto: ");
        String name = input.next();

        System.out.println("Preço do produto: ");
        Double price = input.nextDouble();

        System.out.println("Quantidade em estoque: ");
        int stock = input.nextInt();

        Product product = new Product(name, price, stock);
        products.add(product);

        System.out.println(product.getName() + " cadastrado com sucesso");
        menu();
    }

    private static void listProducts() {
        if (!products.isEmpty()) {
            System.out.println("Lista de produtos: \n");

            for(Product p : products) {
                System.out.println(p);
            }
        } else {
            System.out.println("Nenhum produto cadastrado.");
        }

        menu();
    }

    private static void buyProducts() {
        if (!products.isEmpty()) {
            System.out.println("Produtos disponiveis:");
            for (Product p : products) {
                System.out.println(p + "\n");
            }

            System.out.println("Digite o código do produto: ");
            int id;
            try {
                id = Integer.parseInt(input.next());
            } catch (NumberFormatException e) {
                System.out.println("Código inválido.");
                menu();
                return;
            }

            Product product = null;
            for (Product p : products) {
                if (p.getId() == id) {
                    product = p;
                    break;
                }
            }

            if (product != null) {
                System.out.println("Quantidade que deseja adicionar: ");
                int requestedQuantity;
                try {
                    requestedQuantity = Integer.parseInt(input.next());
                    if (requestedQuantity <= 0) {
                        System.out.println("A quantidade deve ser maior que zero.");
                        buyProducts();
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Quantidade inválida.");
                    buyProducts();
                    return;
                }

                int qtdNoCarrinho = cart.getOrDefault(product, 0);

                if (product.getStock() >= (qtdNoCarrinho + requestedQuantity)) {
                    cart.put(product, qtdNoCarrinho + requestedQuantity);
                    System.out.println(requestedQuantity + " " + product.getName() + "(s) adicionado(s) ao carrinho.");
                    
                    System.out.println("Deseja adicionar mais um produto?");
                    System.out.println("Digite 1 para sim, ou 0 para finalizar a compra.");
                    int option = 0;
                    try {
                        option = Integer.parseInt(input.next());
                    } catch (NumberFormatException e) {
                        // Se for inválido, finaliza a compra por segurança ou volta ao menu
                    }

                    if (option == 1) {
                        buyProducts();
                    } else {
                        finishBuy();
                    }
                } else {
                    System.out.println("Estoque insuficiente. Quantidade disponível: " + (product.getStock() - qtdNoCarrinho));
                    buyProducts();
                }
            } else {
                System.out.println("Produto não encontrado.");
                menu();
            }
        } else {
            System.out.println("Não existem produtos cadastrados.");
            menu();
        }
    }

    private static void seeCart(){
        System.out.println("Produtos no seu carrinho: ");
        if (!cart.isEmpty()) {
            for (Product p : cart.keySet()) {
                System.out.println("Produto: " + p + "\nQuantidade: " + cart.get(p));
            }
        } else {
            System.out.println("Seu carrinho esta vazio");
        }

        menu();
    }

    private static void finishBuy () {
        Double valueBuy = 0.0;
        System.out.println("Seus produtos: ");

        for (Product p : cart.keySet()) {
            int qtd = cart.get(p);
            valueBuy += p.getPrice() * qtd;
            System.out.println(p);
            System.out.println("Quantidade: " + qtd);
        }

        System.out.println("\nO valor da sua compra é: " + Utils.doubleToString(valueBuy));
        
        System.out.println("\nSelecione o método de pagamento:");
        PaymentMethod[] methods = PaymentMethod.values();
        for (int i = 0; i < methods.length; i++) {
            System.out.println((i + 1) + " - " + methods[i].getDescription());
        }

        int methodOption = 0;
        try {
            methodOption = Integer.parseInt(input.next());
        } catch (NumberFormatException e) {
            System.out.println("Opção inválida. Operação cancelada.");
            menu();
            return;
        }

        if (methodOption < 1 || methodOption > methods.length) {
            System.out.println("Opção inválida. Operação cancelada.");
            menu();
            return;
        }

        PaymentMethod selectedMethod = methods[methodOption - 1];

        // Decrement stock only after payment method is selected
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            Product p = entry.getKey();
            int qtd = entry.getValue();
            p.setStock(p.getStock() - qtd);
        }

        System.out.println("\nCompra finalizada com sucesso!");
        System.out.println("Método de Pagamento: " + selectedMethod.getDescription());
        System.out.println("Total: " + Utils.doubleToString(valueBuy));
        
        cart.clear();
        System.out.println("\nObrigado pela preferencia!");
        menu();
    }
;}
