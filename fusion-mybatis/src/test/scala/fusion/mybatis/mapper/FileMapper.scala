package fusion.mybatis.mapper

import fusion.mybatis.model.CFile

trait FileMapper {
  def list(size: Int): java.util.List[CFile]
}
