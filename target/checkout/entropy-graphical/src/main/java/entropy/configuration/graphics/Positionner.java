package entropy.configuration.graphics;

import java.util.List;
import java.util.Map;

/**
 * @author guillaume
 * 
 */
public interface Positionner {

	Map<Element, Disposition> organize(List<Element> l);

}
