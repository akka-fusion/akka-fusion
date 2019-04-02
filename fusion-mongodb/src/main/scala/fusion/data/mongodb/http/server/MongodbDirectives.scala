package fusion.data.mongodb.http.server

import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import akka.http.scaladsl.unmarshalling.Unmarshaller
import org.bson.types.ObjectId

trait MongodbDirectives {
  implicit def objectIdFromStringUnmarshaller: FromStringUnmarshaller[ObjectId] =
    MongodbDirectives._objectIdFromStringUnmarshaller
}

object MongodbDirectives extends MongodbDirectives {
  private val _objectIdFromStringUnmarshaller = Unmarshaller.strict[String, ObjectId](new ObjectId(_))
}
