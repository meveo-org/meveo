/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.meveo.bayad;

import java.util.ArrayList;
import java.util.List;

public class TaskResponse {
	int nbImported = 0;
	int nbRejected = 0;
	int nbIgnored = 0;
	List<String> fileNames = new ArrayList<String>();
	public TaskResponse() {

	}

	public int getNbImported() {
		return nbImported;
	}

	public void setNbImported(int nbImported) {
		this.nbImported = nbImported;
	}

	public int getNbRejected() {
		return nbRejected;
	}

	public void setNbRejected(int nbRejected) {
		this.nbRejected = nbRejected;
	}

	public int getNbIgnored() {
		return nbIgnored;
	}

	public void setNbIgnored(int nbIgnored) {
		this.nbIgnored = nbIgnored;
	}

	public List<String> getFileNames() {
		return fileNames;
	}

	public void setFileNames(List<String> fileNames) {
		this.fileNames = fileNames;
	}

}
