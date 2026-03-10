import java.time.LocalDate;
import java.util.List;

/**
 * Demonstrates the Open/Closed Principle (OCP).
 *
 * - The InvoicePrinter uses the PrintStrategy interface to print invoices.
 * - To support a new output format (e.g., CSV, XML, HTML), you add a new strategy
 *   class implementing PrintStrategy WITHOUT modifying the core InvoicePrinter code.
 *
 * Open for extension: Add new PrintStrategy implementations.
 * Closed for modification: No changes required to InvoicePrinter to support new formats.
 */
public class InvoicePrinter {

    /**
     * Prints the invoice using the provided strategy.
     * This method remains unchanged even if new print formats are introduced.
     */
    public void print(Invoice invoice, PrintStrategy strategy) {
        if (invoice == null) {
            throw new IllegalArgumentException("invoice cannot be null");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("strategy cannot be null");
        }
        strategy.print(invoice);
    }

    // ----- Example usage (can be removed or adapted for tests) -----
    public static void main(String[] args) {
        Invoice invoice = new Invoice(
                "INV-2025-001",
                "Acme Corp",
                LocalDate.now(),
                List.of(
                        new LineItem("Laptop", 1, 1200.00),
                        new LineItem("Mouse", 2, 25.00),
                        new LineItem("Keyboard", 1, 45.50)
                )
        );

        InvoicePrinter printer = new InvoicePrinter();

        // Using different strategies without changing InvoicePrinter:
        printer.print(invoice, new PrettyConsolePrintStrategy());
        System.out.println();
        printer.print(invoice, new JsonPrintStrategy());

        // To extend: create a new class implementing PrintStrategy (e.g., CsvPrintStrategy)
        // and call: printer.print(invoice, new CsvPrintStrategy());
    }
}

/**
 * Abstraction for printing strategies.
 * New implementations can be created without altering InvoicePrinter.
 */
interface PrintStrategy {
    void print(Invoice invoice);
}

/**
 * A human-readable console formatter.
 */
class PrettyConsolePrintStrategy implements PrintStrategy {
    @Override
    public void print(Invoice invoice) {
        System.out.println("====== INVOICE ======");
        System.out.println("Invoice #: " + invoice.number());
        System.out.println("Customer : " + invoice.customerName());
        System.out.println("Date     : " + invoice.date());
        System.out.println("---------------------");
        double total = 0.0;
        for (LineItem item : invoice.items()) {
            double lineTotal = item.quantity() * item.unitPrice();
            total += lineTotal;
            System.out.printf("%-20s x%-3d @ $%-8.2f = $%-8.2f%n",
                    item.description(), item.quantity(), item.unitPrice(), lineTotal);
        }
        System.out.println("---------------------");
        System.out.printf("TOTAL: $%.2f%n", total);
        System.out.println("=====================");
    }
}

/**
 * A simple JSON-like formatter (no external libs).
 */
class JsonPrintStrategy implements PrintStrategy {
    @Override
    public void print(Invoice invoice) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"number\": \"").append(escape(invoice.number())).append("\",\n");
        sb.append("  \"customerName\": \"").append(escape(invoice.customerName())).append("\",\n");
        sb.append("  \"date\": \"").append(invoice.date()).append("\",\n");
        sb.append("  \"items\": [\n");

        double total = 0.0;
        List<LineItem> items = invoice.items();
        for (int i = 0; i < items.size(); i++) {
            LineItem it = items.get(i);
            double lineTotal = it.quantity() * it.unitPrice();
            total += lineTotal;
            sb.append("    {\n");
            sb.append("      \"description\": \"").append(escape(it.description())).append("\",\n");
            sb.append("      \"quantity\": ").append(it.quantity()).append(",\n");
            sb.append("      \"unitPrice\": ").append(String.format("%.2f", it.unitPrice())).append(",\n");
            sb.append("      \"lineTotal\": ").append(String.format("%.2f", lineTotal)).append("\n");
            sb.append("    }");
            if (i != items.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");
        sb.append("  \"total\": ").append(String.format("%.2f", total)).append("\n");
        sb.append("}\n");

        System.out.print(sb.toString());
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

/**
 * Domain model for Invoice. Using records for brevity.
 */
record Invoice(String number, String customerName, LocalDate date, List<LineItem> items) {}

/**
 * Domain model for a line item on the invoice.
 */
record LineItem(String description, int quantity, double unitPrice) {}
