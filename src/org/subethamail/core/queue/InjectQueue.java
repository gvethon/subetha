/*
 * $Id: $
 * $URL:$
 */

package org.subethamail.core.queue;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Binding Type (annotation) for our queues.
 * 
 * This binding type is attached to the configuration 
 * for the Queue, and the injection point it is used.
 * 
 * @author Scott Hernandez
 */

@Qualifier
@Target({FIELD,METHOD,PARAMETER})
@Retention(RUNTIME)
public @interface InjectQueue
{}
