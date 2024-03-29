package eCommerce.ui.admin;

import eCommerce.Constants;
import eCommerce.enums.OrderStatus;
import eCommerce.storage.repositories.interfaces.IDb;
import eCommerce.models.*;
import eCommerce.models.base.User;
import eCommerce.ui.ConsoleUIStrategyBase;
import eCommerce.ui.InputHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static eCommerce.Constants.Navigation.*;

public class AdminUIStrategy extends ConsoleUIStrategyBase {

    public AdminUIStrategy(InputHelper inputHelper, IDb db, User currentUser) {
        super(inputHelper, db, currentUser);
    }

    @Override
    public void printUI() {
        boolean exitFlag = false;

        while(!exitFlag) {
            InputHelper.clearScreen();
            System.out.println("\nДобро пожаловать в систему администрирования магазина:");
            System.out.println("1. Просмотр пользователей");
            System.out.println("2. Просмотр назначенных заказов");
            System.out.println("0. Выход из системы");

            int choice = inputHelper.readInt(0, 2);
            InputHelper.clearScreen();
            switch (choice)
            {
                case Constants.Navigation.ADMIN_USER_MANAGEMENT_CHOICE:
                {
                    System.out.println("Пользователи в системе");

                    ArrayList<Customer> customers = db.getCustomers().getList();
                    for (int i = 1; i <= customers.size(); i++) {
                        System.out.println(i + ". " + customers.get(i - 1).getString() + "\n");
                    }

                    System.out.println("Введите номер пользователя для детального просмотра");
                    System.out.println("Введите " + Constants.Navigation.EXIT_CHOICE + " для выхода");

                    int customerIndex = inputHelper.readInt(EXIT_CHOICE, customers.size());
                    if(customerIndex == Constants.Navigation.EXIT_CHOICE) {
                        break;
                    }

                    Customer targetCustomer = customers.get(customerIndex - 1);
                    InputHelper.clearScreen();

                    System.out.println(targetCustomer.getString());


                    List<Address> userAddresses = db.getAddresses().getListByUserId(targetCustomer.getId());
                    System.out.println("\nАдреса:");
                    for(Address address : userAddresses) {
                        System.out.println(address.getString());
                    }

                    System.out.println("\nЗаказы:");

                    List<Order> userOrders = db.getOrders().getListByAuthorId(targetCustomer.getId());
                    printOrders(userOrders);

                    inputHelper.pressAnyKeyToContinue();
                    break;
                }

                case Constants.Navigation.ADMIN_ORDERS_MANAGEMENT_CHOICE:
                {
                    System.out.println("Назначенные на Вас заказы:");
                    List<Order> orders = db.getOrders().getListByResponsibleAdminIdAndStatus(currentUser.getId(), OrderStatus.created);
                    if(orders.size() == 0) {
                        System.out.println("Назначенных заказов нет:");
                        inputHelper.pressAnyKeyToContinue();
                        break;
                    }
                    printOrders(orders);
                    System.out.println("Введите номер заказа для обработки");
                    System.out.println("Введите " + Constants.Navigation.EXIT_CHOICE + " для выхода");

                    int customerIndex = inputHelper.readInt(0, orders.size());
                    if(customerIndex == Constants.Navigation.EXIT_CHOICE) {
                        break;
                    }

                    Order targetOrder = orders.get(customerIndex - 1);
                    System.out.println("\nЧто вы хотите сделать с заказом: ");
                    System.out.println("1. Закрыть");
                    System.out.println("2. Отменить");
                    System.out.println("0. Назад");

                    int action = inputHelper.readInt(0, 2);
                    switch (action) {
                        case ADMIN_ORDERS_MANAGEMENT_CLOSE: {
                            Bill billingInfo = new Bill(new Date(), targetOrder.getSum(), targetOrder.getId());
                            db.getBills().insert(billingInfo);

                            targetOrder.setOrderStatus(OrderStatus.closed);
                            targetOrder.setBillId(billingInfo.getId());
                            db.getOrders().update(targetOrder);

                            inputHelper.pressAnyKeyToContinue();
                            break;
                        }
                        case ADMIN_ORDERS_MANAGEMENT_CANCEL: {
                            targetOrder.setOrderStatus(OrderStatus.canceled);
                            db.getOrders().update(targetOrder);

                            inputHelper.pressAnyKeyToContinue();
                            break;
                        }
                        case EXIT_CHOICE: {
                            break;
                        }
                    }
                    break;
                }
                case Constants.Navigation.EXIT_CHOICE:
                {
                    exitFlag = true;
                    break;
                }
            }
        }
    }
}
