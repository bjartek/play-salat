package se.radley.plugin

import play.api._
import com.mongodb.casbah.MongoCollection
import com.novus.salat._

package object salat {

  import salat.Formats._

  type Salat = com.novus.salat.annotations.Salat
  type EnumAs = com.novus.salat.annotations.EnumAs
  type Ignore = com.novus.salat.annotations.Ignore
  type Key = com.novus.salat.annotations.Key
  type Persist = com.novus.salat.annotations.Persist

  implicit val ctx = {
    val context = new Context {
      val name = "global"
      override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = "_t")
    }
    context.registerGlobalKeyOverride(remapThis = "id", toThisInstead = "_id")
    context.registerClassLoader(Play.classloader)

    context
  }

  /**
   * get the underlying salat MongoCollection
   * @param collectionName The MongoDB collection name
   * @param sourceName The configured source name
   * @return MongoCollection
   */
  def mongoCollection(collectionName: String, sourceName:String = "default")(implicit app: Application): MongoCollection = {
    app.plugin[SalatPlugin].map(_.collection(collectionName, sourceName)).getOrElse(throw PlayException("SalatPlugin is not registered.", "You need to register the plugin with \"500:se.radley.plugin.salat.SalatPlugin\" in conf/play.plugins"))
  }

  import play.api.data.format._
  import play.api.data.format.Formats._
  /**
   * Formatter to be able to bind ObjectId's in form mappings. 
   * example 
   *  val form = Form(mapping(
   *    "field" -> of[ObjectId]
   *  ))
   **/
  implicit val objectIdFormat = new Formatter[ObjectId] {
   def bind(key: String, data: Map[String, String]) = {
     stringFormat.bind(key, data).right.flatMap { value =>
           scala.util.control.Exception.allCatch[ObjectId]
            .either(new ObjectId(value))
           .left.map(e => Seq(FormError(key, "error.objectId", Nil))) }
   }

   def unbind(key: String, value: ObjectId) = Map(key -> value.toString)
  }
}
