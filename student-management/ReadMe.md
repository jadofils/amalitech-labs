
### Layer Responsibilities

#### 1. **Model Layer** (`model/`)
- Domain entities (Student, RegularStudent, HonorsStudent)
- Pure POJOs with no dependencies on other layers
- Enums for type safety (StudentStatus, StudentType)

#### 2. **Repository Layer** (`repository/` & `repository/impl/`)
- Data access abstraction
- JDBC operations (SQL queries)
- ResultSet to Object mapping
- **Pattern**: Repository Pattern with interface/implementation separation

#### 3. **Service Layer** (`service/` & `serviceimpl/`)
- Business logic orchestration
- Transaction coordination
- Delegates to validation and repository layers
- **Pattern**: Service Layer Pattern

#### 4. **Validation Layer** (`validation/`)
- Input validation rules
- Throws custom exceptions on validation failure
- Called by service layer before persistence

#### 5. **Configuration Layer** (`config/`)
- Database connection management
- Automatic table initialization
- Environment variable loading

#### 6. **Exception Layer** (`exceptions/`)
- Custom exception types
- Meaningful error messages
- Exception hierarchy for different error categories

---

## 🗄️ Database Schema

### Tables

#### **students**
```sql
CREATE TABLE students (
    student_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    student_type VARCHAR(255) NOT NULL
);
```