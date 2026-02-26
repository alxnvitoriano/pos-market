package main;

import model.Product;
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

        Product product = new Product(name, price);
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
        if(!products.isEmpty()) {
            System.out.println("Código do produtos: \n");

            System.out.println("Produtos disponiveis:");
            for (Product p : products) {
                System.out.println(p + "\n");
            }
            int id = Integer.parseInt(input.next());
            boolean isPresent = false;

            for (Product p : products) {
                if (p.getId() == id) {
                    int qtd = 0;
                    try {
                        qtd = cart.get(p);
                        // check if the product is in the cart, increase quantity
                        cart.put(p, qtd + 1);
                    } catch (NullPointerException e) {
                        //if the product is the first in cart, received 1
                        cart.put(p, 1);
                    }
                    System.out.println(p.getName() + " adicionado ao carrinho.");
                    isPresent = true;

                    if (isPresent) {
                        System.out.println("Deseja adicionar mais um produto?");
                        System.out.println("Digite 1 para sim, ou 0 para finalizar a compra.");
                        int option = Integer.parseInt(input.next());

                        if (option == 1) {
                            buyProducts();
                        } else {
                            finishBuy();
                        }
                    }
                } else {
                    System.out.println("Produto não encontrado.");
                    menu();
                }
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
        System.out.println("O valor da sua compra é: " + Utils.doubleToString(valueBuy));
        cart.clear();
        System.out.println("Obrigado pela preferencia!");
        menu();
    }
;}
