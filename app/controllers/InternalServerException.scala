package controllers

case class InternalServerException(message: String) extends Throwable(message)