# Inventory Management with MRP

This is a demo / toy project for inventory management with MRP capabilities.

## MRP

MRP (Materials Requirements Planning) in a naive notion is basically to acquire / buy your stocks
based on the BOM (Bill of Materials). Accordingly, conventional re-order point re-stocking, that does not
consider BOM, can run into issues of some parts running out too fast. While some low usage parts could be in surplus.

This app attempts to compute the required quantity of parts based on a BOM, and make some lead time estimates.
But for its purposes, it is a bare minimum, naive 'MRP'. 

It is more of / intended as a demo / toy to experiment with 'MRP' concepts 

## Features

- **Product Management**: Add, edit, and delete products with support for both components and assemblies
- **Bill of Materials (BOM)**: Define hierarchical product structures with parent-child relationships
- **Inventory Tracking**: Track stock movements with transactions (IN, OUT, ADJUSTMENT)
- **MRP Calculation**: Calculate material requirements for dependent demand based on BOM
- **Lead time estimates**: lead time = order lead time + quantity x item lead time, 
  based on the longest lead time for each of the parts
- **Purchase Order Management**: Generate and manage purchase orders based on stock shortages
- **Swing UI**: User-friendly graphical interface for all operations

## Technology Stack

- **Java**: 11
- **Build System**: Maven
- **Database**: H2 (embedded)
- **Database Migration**: Flyway
- **ORM**: sql2o
- **UI Framework**: Swing
- **Testing**: JUnit 5

App is created with aid of Github Copilot.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/inventorymrp/
│   │       ├── model/          # Domain models (Product, BOMItem, etc.)
│   │       ├── dao/            # Data Access Objects
│   │       ├── service/        # Business logic (MRP, Inventory)
│   │       ├── ui/             # Swing UI panels
│   │       └── util/           # Utilities (DatabaseUtil)
│   └── resources/
└── test/
    └── java/
        └── com/inventorymrp/   # Unit and integration tests
```

## Models

### Product
Represents inventory items (components or assemblies):
- Code, name, description
- Unit cost, stock quantity
- Reorder level, lead time
- Assembly flag

### BOMItem
Represents Bill of Materials relationships:
- Parent product (assembly)
- Child product (component)
- Quantity required per parent
- Sequence number

### InventoryTransaction
Tracks stock movements:
- Transaction type (IN, OUT, ADJUSTMENT)
- Product, quantity
- Reference, timestamp

### PurchaseOrder
Manages procurement:
- Product, quantity
- Status (PENDING, ORDERED, RECEIVED, CANCELLED)
- Expected delivery date, supplier

## Build and Run

### Build the project:
```bash
mvn clean package
```

### Run tests:
```bash
mvn test
```

### Run the application:
```bash
java -jar target/jinventorymrp-1.0.0-SNAPSHOT.jar
```

Or:
```bash
mvn exec:java -Dexec.mainClass="com.inventorymrp.ui.MainApp"
```

## Usage

### 1. Products Tab
- Add new products (components or assemblies)
- Edit existing products
- Manage stock levels, costs, and lead times

### 2. BOM Tab
- Define Bill of Materials for assemblies
- Specify quantity of each component needed
- View BOM by product

### 3. Inventory Tab
- Add stock (receive goods)
- Remove stock (production consumption)
- Adjust stock (corrections, cycle counts)
- View transaction history

### 4. MRP Tab
- Select a product and demand quantity
- Calculate material requirements
- Check material availability
- Generate purchase orders for shortages

### 5. Purchase Orders Tab
- View all purchase orders
- Update order status
- Track expected delivery dates

## MRP Example

1. Create an assembly product "BIKE" with `isAssembly=true`
2. Create component products "WHEEL", "FRAME", "CHAIN"
3. Add BOM items:
   - BIKE requires 2 x WHEEL
   - BIKE requires 1 x FRAME
   - BIKE requires 1 x CHAIN
4. Set initial stock levels for components
5. In MRP tab, calculate requirements to build 10 BIKES
6. System calculates: 20 WHEELS, 10 FRAMES, 10 CHAINS needed
7. Generate purchase orders for any shortages

## Database

The application uses an embedded H2 database stored in `./data/inventorydb.mv.db`. Database schema is managed by **Flyway** for version-controlled migrations.

### Database Migrations

Migration scripts are located in `src/main/resources/db/migration/`:
- `V1__Create_products_table.sql` - Products table
- `V2__Create_bom_items_table.sql` - Bill of Materials table
- `V3__Create_inventory_transactions_table.sql` - Inventory transactions table
- `V4__Create_purchase_orders_table.sql` - Purchase orders table

Flyway automatically applies migrations on application startup. To reset the database, simply delete the `data` directory.

## Testing

The project includes comprehensive unit and integration tests:
- **Model Tests**: Validate domain object behavior
- **DAO Tests**: Test database operations
- **Service Tests**: Verify business logic including MRP calculations

All tests use in-memory H2 databases for isolation.

## NO WARRANTY

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.




