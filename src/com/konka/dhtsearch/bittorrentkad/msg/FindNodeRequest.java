package com.konka.dhtsearch.bittorrentkad.msg;

import com.konka.dhtsearch.Key;
import com.konka.dhtsearch.Node;

/**
 * A findNode request as defined in the kademlia protocol
 * 
 * @author eyal.kibbar@gmail.com
 *
 */
public class FindNodeRequest extends KadRequest {

	private static final long serialVersionUID = -7084922793331210968L;
	private Key key;
	private boolean searchCache;

	FindNodeRequest(long id, Node src) {
		super(id, src);
	}

	/**
	 * 
	 * @return the key we are searching
	 */
	public Key getKey() {
		return key;
	}

	public FindNodeRequest setKey(Key key) {
		this.key = key;
		return this;
	}

	@Override
	public FindNodeResponse generateResponse(Node localNode) {
		return new FindNodeResponse(getId(), localNode);
	}

	public FindNodeRequest setSearchCache(boolean searchCache) {
		this.searchCache = searchCache;
		return this;
	}

	public boolean shouldSearchCache() {
		return searchCache;
	}

}
