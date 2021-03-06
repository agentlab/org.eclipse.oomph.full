/*
 * Copyright (c) 2014 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */

#ifndef ORG_ECLIPSE_OOMPH_JREINFO_LIB_SRC_JRES_H_
#define ORG_ECLIPSE_OOMPH_JREINFO_LIB_SRC_JRES_H_

#include "eclipseUnicode.h"

typedef struct jre_s {
	_TCHAR* javaHome;
	int jdk;
	struct jre_s* next;
} JRE;

JRE* findAllJREs();

#endif /* ORG_ECLIPSE_OOMPH_JREINFO_LIB_SRC_JRES_H_ */
