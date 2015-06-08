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

#ifndef EXTRACTOR_H_
#define EXTRACTOR_H_

#define EXIT_CANCEL -1
#define EXIT_FAILURE_JRE_OUTPUT -2
#define EXIT_FAILURE_JRE_DETECTION -3
#define EXIT_FAILURE_PRODUCT_DESCRIPTION -4
#define EXIT_FAILURE_PRODUCT_EXTRACTION -5

byte marker[] = { 0, 84, 72, 73, 83, 32, 73, 83, 32, 84, 72, 69, 32, 77, 65, 82, 75, 69, 82, 32, 83, 84, 82, 73, 78, 71, 32, 70, 79, 82, 32, 84, 72, 69, 32,
    69, 67, 76, 73, 80, 83, 69, 32, 79, 79, 77, 80, 72, 32, 69, 88, 84, 82, 65, 67, 84, 79, 82, 32, 49, 57, 55, 48, 45, 49, 50, 45, 48, 51, 32, };

#endif /* EXTRACTOR_H_ */
