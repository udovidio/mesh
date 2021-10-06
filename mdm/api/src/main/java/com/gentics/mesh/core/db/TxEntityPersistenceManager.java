package com.gentics.mesh.core.db;

import com.gentics.mesh.core.data.HibBaseElement;
import com.gentics.mesh.core.data.HibCoreElement;
import com.gentics.mesh.core.data.dao.Dao;
import com.gentics.mesh.core.data.dao.RootDao;
import com.gentics.mesh.core.rest.common.RestModel;

/**
 * A set of low level entity persistence management methods.
 * 
 * @author plyhun
 *
 */
public interface TxEntityPersistenceManager {
	
	/**
	 * Create a new persisted entity with the given optional uuid. 
	 * If uuid parameter is null, a new generated UUID will be used.
	 * Prefer {@link TxEntityPersistenceManager#create(String, Dao)} over this method, 
	 * to keep the creation business logic. This API method serves test purposes.
	 * 
	 * @param <T>
	 * @param uuid 
	 * @param classOfT the persistence class to use
	 * @return
	 */
	<T extends HibBaseElement> T create(String uuid, Class<? extends T> classOfT);
	
	/**
	 * Merge the data from given POJO into the persistent entity.
	 * Prefer {@link TxEntityPersistenceManager#persist(HibCoreElement, Dao)} over this method, 
	 * to keep the persistence business logic. This API method serves test purposes.
	 * 
	 * @param element
	 * @param classOfT the persistence class to use
	 * @return
	 */
	<T extends HibBaseElement> T persist(T element, Class<? extends T> classOfT);
	
	/**
	 * Delete the persistent entity.
	 * Prefer {@link TxEntityPersistenceManager#delete(HibCoreElement, Dao)} over this method, 
	 * to keep the deletion business logic. This API method serves test purposes.
	 * 
	 * @param element
	 * @param classOfT the persistence class to use
	 */
	<T extends HibBaseElement> void delete(T element, Class<? extends T> classOfT);

	/**
	 * Create a new persisted core entity with the given optional uuid. 
	 * If uuid parameter is null, a new generated UUID will be used
	 * 
	 * @see HibCoreElement
	 * @param <T>
	 * @param uuid 
	 * @param dao DAO for the persistence class to use for the possible higher level actions
	 * @return
	 */
	<T extends HibCoreElement<? extends RestModel>> T create(String uuid, Dao<T> dao);
	
	/**
	 * Merge the data from given POJO into the persistent core entity.
	 * 
	 * @see HibCoreElement
	 * @param element
	 * @param dao DAO for the persistence class to use for the possible higher level actions
	 * @return
	 */
	<T extends HibCoreElement<? extends RestModel>> T persist(T element, Dao<T> dao);
	
	/**
	 * Delete the persistent core entity.
	 * 
	 * @see HibCoreElement
	 * @param element
	 * @param dao DAO for the persistence class to use for the possible higher level actions
	 */
	<T extends HibCoreElement<? extends RestModel>> void delete(T element, Dao<T> dao);

	/**
	 * Create new uninitialized persisted element within the given root. 
	 * 
	 * @param root
	 * @param uuid if null, the generated UUID will be attached to the created element.
	 * @param dao DAO for the persistence class to use for the possible higher level actions
	 * @return
	 */
	<R extends HibCoreElement<? extends RestModel>, L extends HibCoreElement<? extends RestModel>> L createInRoot(R root, String uuid, RootDao<R, L> dao);
	
	/**
	 * Merge the element data into its persistent state within the given root.
	 * 
	 * @param root
	 * @param element
	 * @param dao DAO for the persistence class to use for the possible higher level actions
	 * @return
	 */
	<R extends HibCoreElement<? extends RestModel>, L extends HibCoreElement<? extends RestModel>> L persistInRoot(R root, L element, RootDao<R, L> dao);
	
	/**
	 * Delete the persistent entity itself, removing it from the root.
	 * 
	 * @param root
	 * @param element
	 * @param dao DAO for the persistence class to use for the possible higher level actions
	 */
	<R extends HibCoreElement<? extends RestModel>, L extends HibCoreElement<? extends RestModel>> void deleteInRoot(R root, L element, RootDao<R, L> dao);
}
