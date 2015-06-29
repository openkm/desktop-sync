package com.openkm.okmsynchronize.ws;

import com.openkm.okmsynchronize.utils.SynchronizeException;
import com.openkm.sdk4j.bean.Document;
import com.openkm.sdk4j.bean.Folder;
import java.io.InputStream;
import java.util.List;

public interface OpenKMWS {

	public boolean isConnectionSuccessful();

	public boolean exists(String node, String object) throws SynchronizeException;

	public boolean isFolder(String node) throws SynchronizeException;

	public void createFolder(String node) throws SynchronizeException;

	public void deleteFolder(String node) throws SynchronizeException;

	public List<Folder> listFolders(String node) throws SynchronizeException;

	public Folder getFodler(String node) throws SynchronizeException;

	public void renameFolder(String node, String newName) throws SynchronizeException;

	public boolean isDocument(String node) throws SynchronizeException;

	public String getNodeUuid(String uuid) throws SynchronizeException;

	public Document getDocument(String node) throws SynchronizeException;

	public void deleteDocument(String node) throws SynchronizeException;

	public byte[] getContentDocument(String node) throws SynchronizeException;

	public void setContentDocument(String node, InputStream is, String comment) throws SynchronizeException;

	public List<Document> listDocuments(String node) throws SynchronizeException;

	public void renameDocument(String node, String newName) throws SynchronizeException;

	public String getRootNode(String context) throws SynchronizeException;
}
