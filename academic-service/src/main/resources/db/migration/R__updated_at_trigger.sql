DROP TRIGGER IF EXISTS set_updated_at_trigger ON users;

CREATE TRIGGER set_updated_at_trigger
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
